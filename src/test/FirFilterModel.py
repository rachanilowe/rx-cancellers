import random
import matplotlib.pyplot as plt

# For consistent testing
random.seed(10)

# Parameters
N = 1000               # Number of samples
L = 6                  # FIR filter taps
D = 2                  # Delay for DLMS
scale = 128            # Scaling y

# Signals: Integer values
tx_data = [random.randint(-32, 31) for _ in range(N)]
remote_signal_clean = [random.randint(-32, 31) for _ in range(N)]

noise = [(3 * tx_data[i]) // 2 for i in range(N)]

remote_signal = [remote_signal_clean[i] + noise[i] for i in range(N)]

w = [0] * L
cleaned = []
output = []

# Input buffer for Tx_data history
x_buffer = []

for T in range(N):
    x_buffer.insert(0, tx_data[T])  
    if len(x_buffer) > D + L - 1:
        x_buffer.pop() 

    if T >= D:
        # Construct delayed input vector x[n - D - i] for i = 0 to L-1
        x_T_D = []
        for i in range(L):
            idx = D + i
            if idx < len(x_buffer):
                x_T_D.append(x_buffer[idx])
            else:
                x_T_D.append(0)  # Zero-padding if not enough history

        # Compute FIR output y(T - D)
        y_T_D = sum([w[i] * x_T_D[i] for i in range(L)]) // scale
        output.append(y_T_D)

        # Error signal
        d_T_D = remote_signal[T - D]
        e_T_D = d_T_D - y_T_D
        cleaned.append(e_T_D)

        print(f"Step {T}: Input - {tx_data[T]}, Desired - {remote_signal[T]}, Original Desired - {remote_signal_clean[T]}, Weights - {w}, Filtered - {cleaned[T]}")

        # Weight update - Tap-leakage DLMS
        # Keep similar to what's in FIRSegment
        for i in range(L):
            w[i] = (((w[i] * 511) >> 9) + ((e_T_D * x_T_D[i]) >> 6))
    else:
        output.append(0)
        cleaned.append(0)

plt.figure(figsize=(12, 6))
plt.plot(remote_signal_clean[900:], label="Clean remote_signal (desired)")
plt.plot(remote_signal[900:], label="Noisy remote_signal", alpha=0.5)
plt.plot(cleaned[902:], label="Cleaned output")   # Delayed for nicer comparison with the original signal
plt.legend()
plt.title("DLMS Echo Cancellation")
plt.xlabel("Clock Step")
plt.ylabel("Output Value")
plt.grid(True)
plt.tight_layout()
plt.show()