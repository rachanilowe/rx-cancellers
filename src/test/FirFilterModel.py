import random
import matplotlib.pyplot as plt
import numpy as np

from itertools import product

# For consistent testing
random.seed(10)

# Use these values to generate FIR filter "configs"
# Only add a few for plotting the MSEs to prevent graph from becoming crowded!
echo_tap_counts = [8, 12]
next_tap_counts = [8, 12]
D_values = [1, 3, 4, 5, 6, 7, 8, 9, 11, 13, 15, 17, 19, 24, 29, 34]
scale_values = [512, 1024, 2048]

echo_gamma_values = [2e-3, 2e-4, 2e-5, 2e-6, 2e-7, 2e-8]
echo_mu_values = [0.5, 0.75, 0.25, 0.125, 0.625, 0.375, 0.1875, 0.8125, 2e-6]
next_gamma_values = [2e-3, 2e-4, 2e-5, 2e-6, 2e-7, 2e-8]      # Shared with the three NEXT cancellers
next_mu_values = [0.5, 0.75, 0.25, 0.125, 0.625, 0.375, 0.1875, 0.8125, 2e-6]

# Parameters
N = 1000            # Number of samples

param_sets = []

# for echo_tap_count, next_tap_count, delay, scale, echo_gamma, echo_mu, next_gamma, next_mu in product(echo_tap_counts, next_tap_counts, D_values, scale_values, echo_gamma_values, echo_mu_values, next_gamma_values, next_mu_values):
#     if echo_tap_count % (delay + 1) == 0 and next_tap_count % (delay + 1) == 0 and echo_tap_count // (delay + 1) <= 2 and next_tap_count // (delay + 1) <= 2 and echo_gamma * echo_mu < 1 and next_gamma * next_mu < 1:
#         param_sets.append({'echo_tap_count': echo_tap_count, 'next_tap_count': next_tap_count, 'D_tx': delay, 'D_ch': delay, 'scale': scale, 'echo_gamma': echo_gamma, 'echo_mu': echo_mu, 'next_gamma': next_gamma, 'next_mu': next_mu})

# Or can add specific parameters to param_sets
# Examples:
param_sets.append({'echo_tap_count': 12, 'next_tap_count': 12, 'delay': 5, 'scale': 512, 'echo_gamma': 0.0625, 'echo_mu': 0.5625, 'next_gamma': 0.1875, 'next_mu': 0.5625})
param_sets.append({'echo_tap_count': 18, 'next_tap_count': 18, 'delay': 17, 'scale': 2048, 'echo_gamma': 0.001953125, 'echo_mu': 0.5, 'next_gamma': 0.001953125, 'next_mu': 0.5})

best_mse = float("inf")
best_config = None

# Key: config, Value: MSE values as List
mse_data = {}

# TX Data: sent from current controller
# Channel Data 1 - 3: TX Data from the other three controllers
# remote_signal_clean: Clean RX data
tx_data = [random.randint(-4, 3) for _ in range(N)]
channel_data_1 = [random.randint(-4, 3) for _ in range(N)]
channel_data_2 = [random.randint(-4, 3) for _ in range(N)]
channel_data_3 = [random.randint(-4, 3) for _ in range(N)]
remote_signal_clean = [random.randint(-4, 3) for _ in range(N)]
print("Running sims")
for idx, params in enumerate(param_sets):
    echo_tap_count, next_tap_count, delay, scale, echo_gamma, echo_mu, next_gamma, next_mu = params.values()
    # print(params.values())

    # TODO: verify echo + NEXT noise model
    noise_tx = [tx_data[i] >> 1 for i in range(N)]
    noise_1  = [channel_data_1[i] >> 2 for i in range(N)]
    noise_2  = [channel_data_2[i] >> 2 for i in range(N)]
    noise_3  = [channel_data_3[i] >> 2 for i in range(N)]

    remote_signal = [remote_signal_clean[i] + noise_tx[i] + noise_1[i] + noise_2[i] + noise_3[i] for i in range(N)]

    # Weights
    w_tx = [0] * echo_tap_count
    w_1 = [0] * next_tap_count
    w_2 = [0] * next_tap_count
    w_3 = [0] * next_tap_count

    # Weight history : w(i+1) = (1-gamma)w(i) + mu * e(i-D) * x(i-D)
    # e(i-D) = desired(i-D) - firOutput(i-D)
    delayed_w_tx = [w_tx.copy()]
    delayed_w_1 = [w_1.copy()]
    delayed_w_2 = [w_2.copy()]
    delayed_w_3 = [w_3.copy()]

    # Buffers
    x_tx_buffer = []
    x_1_buffer = []
    x_2_buffer = []
    x_3_buffer = []

    output = []
    cleaned = []

    local_mse = []

    for T in range(N):
        # Update buffers
        x_tx_buffer.insert(0, tx_data[T])
        x_1_buffer.insert(0, channel_data_1[T])
        x_2_buffer.insert(0, channel_data_2[T])
        x_3_buffer.insert(0, channel_data_3[T])

        # Trim buffers
        max_D_L = max(delay + echo_tap_count, delay + next_tap_count)
        if len(x_tx_buffer) > max_D_L:
            x_tx_buffer.pop()
            x_1_buffer.pop()
            x_2_buffer.pop()
            x_3_buffer.pop()

        if T >= delay:
            def get_delayed_vector(buf, delay, taps):
                return [buf[delay + i] if delay + i < len(buf) else 0 for i in range(taps)]

            x_T_D_tx = get_delayed_vector(x_tx_buffer, delay, echo_tap_count)
            x_T_D_1  = get_delayed_vector(x_1_buffer, delay, next_tap_count)
            x_T_D_2  = get_delayed_vector(x_2_buffer, delay, next_tap_count)
            x_T_D_3  = get_delayed_vector(x_3_buffer, delay, next_tap_count)

            w_tx_d = delayed_w_tx[-delay - 1] if len(delayed_w_tx) > delay else [0] * echo_tap_count
            w_1_d  = delayed_w_1[-delay - 1] if len(delayed_w_1) > delay else [0] * next_tap_count
            w_2_d  = delayed_w_2[-delay - 1] if len(delayed_w_2) > delay else [0] * next_tap_count
            w_3_d  = delayed_w_3[-delay - 1] if len(delayed_w_3) > delay else [0] * next_tap_count

            y_tx = sum(w_tx_d[i] * x_T_D_tx[i] for i in range(echo_tap_count)) // scale
            y_1  = sum(w_1_d[i]  * x_T_D_1[i]  for i in range(next_tap_count)) // scale
            y_2  = sum(w_2_d[i]  * x_T_D_2[i]  for i in range(next_tap_count)) // scale
            y_3  = sum(w_3_d[i]  * x_T_D_3[i]  for i in range(next_tap_count)) // scale
            total_y = y_tx + y_1 + y_2 + y_3
            output.append(total_y)

            e_T_D = remote_signal[T - delay] - total_y
            cleaned.append(e_T_D)

            error_tx = remote_signal[T - delay] - y_tx
            error_1 = remote_signal[T - delay] - y_1
            error_2 = remote_signal[T - delay] - y_2
            error_3 = remote_signal[T - delay] - y_3

            for i in range(echo_tap_count):
                w_tx[i] = (int((1-(echo_gamma * echo_mu)) * (w_tx[i])) + int((error_tx * x_T_D_tx[i]) * echo_mu))

            for i in range(next_tap_count):
                w_1[i] = (int((1-(next_gamma * next_mu)) * (w_1[i])) + int((error_1 * x_T_D_1[i]) * next_mu))

            for i in range(next_tap_count):
                w_2[i] = (int((1-(next_gamma * next_mu)) * (w_2[i])) + int((error_2 * x_T_D_2[i]) * next_mu))

            for i in range(next_tap_count):
                w_3[i] = (int((1-(next_gamma * next_mu)) * (w_3[i])) + int((error_3 * x_T_D_3[i]) * next_mu))
                
            delayed_w_tx.append(w_tx.copy())
            delayed_w_1.append(w_1.copy())
            delayed_w_2.append(w_2.copy())
            delayed_w_3.append(w_3.copy())
        else:
            output.append(0)
            cleaned.append(0)
    for T in range(N):
        mse = np.mean([(remote_signal_clean[i] - cleaned[i + delay])**2 for i in range(0, max(0, (T - delay)))])
        local_mse.append(mse)

    mse_data[f'Echo Len: {echo_tap_count}, NEXT Len: {next_tap_count}, Delay: {delay}, Scale: {scale}, Echo Gamma: {echo_gamma}, Echo Mu: {echo_mu}, NEXT Gamma: {next_gamma}, NEXT Mu: {next_mu}'] = local_mse

for key, val in mse_data.items():
    plt.plot(val, label=key)

plt.title('MSE Curves')
plt.xlabel('Clock Step')
plt.ylabel('MSE')
plt.legend()
plt.grid(True)
plt.show()
