import random
import matplotlib.pyplot as plt
random.seed(10)

# Parameters
N = 1000               # Number of samples
L = 6                  # FIR filter taps
D = 2                  # Delay for DLMS
scale = 128            # Fixed-point scaling
mu = 2**-6             # Step size
gamma = 2**-9          # Leakage factor

mu_fp = int(mu * scale)
gamma_fp = int(gamma * scale)

# Signals: Integer values
Tx_data = [random.randint(-32, 31) for _ in range(N)]
remoteSignal_clean = [random.randint(-32, 31) for _ in range(N)]

# Simulate realistic NEXT (approx. -36 dB → scale Tx_data by ~1/64)
NEXT_interference = [(3 * Tx_data[i]) // 2 for i in range(N)]

remoteSignal = [remoteSignal_clean[i] + NEXT_interference[i] for i in range(N)]

# Initialize
w = [0] * L
cleaned = []
output = []

# Input buffer for Tx_data history
x_buffer = []

for T in range(N):
    x_buffer.insert(0, Tx_data[T])  # Push latest input to front
    if len(x_buffer) > D + L - 1:
        x_buffer.pop()  # Maintain size

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
        d_T_D = remoteSignal[T - D]
        e_T_D = d_T_D - y_T_D
        cleaned.append(e_T_D)

        print(f"Step {T}: Input - {Tx_data[T]}, Desired - {remoteSignal[T]}, Original Desired - {remoteSignal_clean[T]}, Weights - {w}, Filtered - {cleaned[T]}")

        # Weight update
        for i in range(L):
            w[i] = (((w[i] * 511) >> 9) + ((e_T_D * x_T_D[i]) >> 6))
    else:
        output.append(0)
        cleaned.append(0)

# Plot
plt.figure(figsize=(12, 6))
plt.plot(remoteSignal_clean[900:], label="Clean remoteSignal (desired)")
plt.plot(remoteSignal[900:], label="Noisy remoteSignal (with NEXT)", alpha=0.5)
plt.plot(cleaned[902:], label="Cleaned output (DLMS estimate)")
plt.legend()
plt.title("DLMS Echo Cancellation with Proper Delay Alignment")
plt.xlabel("Time")
plt.ylabel("Amplitude")
plt.grid(True)
plt.tight_layout()
plt.show()