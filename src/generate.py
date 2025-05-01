import random
import csv

num_points = 500
random.seed(42)  # For reproducibility

remote = [random.randint(-2, 5) for _ in range(num_points)]
tx0 = [random.randint(-1, 3) for _ in range(num_points)]
tx1 = [random.randint(-1, 3) for _ in range(num_points)]
tx2 = [random.randint(-1, 3) for _ in range(num_points)]
tx3 = [random.randint(-1, 3) for _ in range(num_points)]

with open('random_signals_patterned.csv', 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['remote', 'tx0', 'tx1', 'tx2', 'tx3'])
    for i in range(num_points):
        if i == 0:
            writer.writerow([remote[i], tx0[i], tx1[i], tx2[i], tx3[i]])
        else: 
            r_sum = remote[i-1] + remote[i]
            t0_sum = tx0[i-1] + tx0[i]
            t1_sum = tx1[i-1] + tx1[i]
            t2_sum = tx2[i-1] + tx2[i]
            t3_sum = tx3[i-1] + tx3[i]
            # Clamp remote between -128 and 121
            r_sum = max(-128, min(127, r_sum))

            # Clamp tx signals between -32 and 31
            t0_sum = max(-32, min(31, t0_sum))
            t1_sum = max(-32, min(31, t1_sum))
            t2_sum = max(-32, min(31, t2_sum))
            t3_sum = max(-32, min(31, t3_sum))

            writer.writerow([r_sum, t0_sum, t1_sum, t2_sum, t3_sum])