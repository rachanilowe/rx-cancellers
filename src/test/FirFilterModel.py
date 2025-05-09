import random
import matplotlib.pyplot as plt

# For consistent testing
random.seed(10)

N = 1000            # Number of samples
L_tx = 18           # FIR filter taps for tx_data
L_ch = 18           # FIR filter taps for channel_data
D_tx = 17            # Delay for tx_data canceller
D_ch = 17            # Delay for channel_data cancellers
scale = 2048         # Scaling output

# TX Data: sent from current controller
# Channel Data 1 - 3: TX Data from the other three controllers
# remote_signal_clean: Clean RX data
tx_data = [random.randint(-4, 3) for _ in range(N)]
channel_data_1 = [random.randint(-4, 3) for _ in range(N)]
channel_data_2 = [random.randint(-4, 3) for _ in range(N)]
channel_data_3 = [random.randint(-4, 3) for _ in range(N)]
remote_signal_clean = [random.randint(-4, 3) for _ in range(N)]

# TODO: verify echo + NEXT noise model
noise_tx = [tx_data[i] >> 1 for i in range(N)]
noise_1  = [channel_data_1[i] >> 2 for i in range(N)]
noise_2  = [channel_data_2[i] >> 2 for i in range(N)]
noise_3  = [channel_data_3[i] >> 2 for i in range(N)]

remote_signal = [(remote_signal_clean[i] ) + noise_tx[i] + noise_1[i] + noise_2[i] + noise_3[i] for i in range(N)]

# Weights
w_tx = [0] * L_tx
w_1 = [0] * L_ch
w_2 = [0] * L_ch
w_3 = [0] * L_ch

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

maxWeight = 0

for T in range(N):
    # Update buffers
    x_tx_buffer.insert(0, tx_data[T])
    x_1_buffer.insert(0, channel_data_1[T])
    x_2_buffer.insert(0, channel_data_2[T])
    x_3_buffer.insert(0, channel_data_3[T])

    # Trim buffers
    max_D_L = max(D_tx + L_tx, D_ch + L_ch)
    if len(x_tx_buffer) > max_D_L:
        x_tx_buffer.pop()
        x_1_buffer.pop()
        x_2_buffer.pop()
        x_3_buffer.pop()

    if T >= max(D_tx, D_ch):
        def get_delayed_vector(buf, delay, taps):
            return [buf[delay + i] if delay + i < len(buf) else 0 for i in range(taps)]

        x_T_D_tx = get_delayed_vector(x_tx_buffer, D_tx, L_tx)
        x_T_D_1  = get_delayed_vector(x_1_buffer, D_ch, L_ch)
        x_T_D_2  = get_delayed_vector(x_2_buffer, D_ch, L_ch)
        x_T_D_3  = get_delayed_vector(x_3_buffer, D_ch, L_ch)

        w_tx_d = delayed_w_tx[-D_tx - 1] if len(delayed_w_tx) > D_tx else [0] * L_tx
        w_1_d  = delayed_w_1[-D_ch - 1] if len(delayed_w_1) > D_ch else [0] * L_ch
        w_2_d  = delayed_w_2[-D_ch - 1] if len(delayed_w_2) > D_ch else [0] * L_ch
        w_3_d  = delayed_w_3[-D_ch - 1] if len(delayed_w_3) > D_ch else [0] * L_ch

        y_tx = sum(w_tx_d[i] * x_T_D_tx[i] for i in range(L_tx)) // scale
        y_1  = sum(w_1_d[i]  * x_T_D_1[i]  for i in range(L_ch)) // scale
        y_2  = sum(w_2_d[i]  * x_T_D_2[i]  for i in range(L_ch)) // scale
        y_3  = sum(w_3_d[i]  * x_T_D_3[i]  for i in range(L_ch)) // scale
        total_y = y_tx + y_1 + y_2 + y_3
        output.append(total_y)

        e_T_D = remote_signal[T - max(D_tx, D_ch)] - total_y

        error_tx = remote_signal[T - max(D_tx, D_ch)] - y_tx
        error_1 = remote_signal[T - max(D_tx, D_ch)] - y_1
        error_2 = remote_signal[T - max(D_tx, D_ch)] - y_2
        error_3 = remote_signal[T - max(D_tx, D_ch)] - y_3
        cleaned.append(e_T_D)

        print(f"Weights 0: {delayed_w_tx[-D_tx - 1] if len(delayed_w_tx) > D_tx else 0}")
        print(f"Weights 1: {delayed_w_1[-D_ch - 1] if len(delayed_w_1) > D_ch else 0}")
        print(f"Weights 2: {delayed_w_2[-D_ch - 1] if len(delayed_w_2) > D_ch else 0}")
        print(f"Weights 3: {delayed_w_3[-D_ch - 1] if len(delayed_w_3) > D_ch else 0}")
        for i in range(L_tx):
            w_tx[i] = (int((1-(0.001953125 * 0.375)) * (w_tx[i])) + int((error_tx * x_T_D_tx[i]) * 0.375))
        for i in range(L_ch):
            w_1[i] = (int((1-(0.09375 * 0.375)) * (w_1[i])) + int((error_1 * x_T_D_1[i]) * 0.375))      
        for i in range(L_ch):
            w_2[i] = (int((1-(0.09375 * 0.375)) * (w_2[i])) + int((error_2 * x_T_D_2[i]) * 0.375))      
        for i in range(L_ch):
            w_3[i] = (int((1-(0.09375 * 0.375)) * (w_3[i])) + int((error_3 * x_T_D_3[i]) * 0.375))      

        # Printing out the largest weight
        maxWeight = max(max(w_tx + w_1 + w_2 + w_3), maxWeight)
        delayed_w_tx.append(w_tx.copy())
        delayed_w_1.append(w_1.copy())
        delayed_w_2.append(w_2.copy())
        delayed_w_3.append(w_3.copy())
    else:
        output.append(0)
        cleaned.append(0)
    print(f"Step {T}: Original={remote_signal_clean[T]}, Input0={tx_data[T]}, Input1={channel_data_1[T]}, Input2={channel_data_2[T]}, Input3={channel_data_3[T]}, Desired={remote_signal[T]}, Error={cleaned[T]}")
print(maxWeight)
# Plot
plt.figure(figsize=(12, 6))
plt.plot(remote_signal_clean[700:800], label="Clean remote_signal (desired)")
plt.plot(remote_signal[700:800], label="Noisy remote_signal", alpha=0.5)
plt.plot(cleaned[700 + max(D_tx, D_ch):800], label="Cleaned output")
plt.legend()
plt.title("Echo Cancellation")
plt.xlabel("Clock Step")
plt.ylabel("Signal Value")
plt.grid(True)
plt.tight_layout()
plt.show()
