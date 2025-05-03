import random
import csv

num_points = 500
random.seed(42)  # For reproducibility

# Generate random values for each column with specified ranges
remote = [random.randint(-100, 100) for _ in range(num_points)]
tx0 = [random.randint(-32, 31) for _ in range(num_points)]
tx1 = [random.randint(-32, 31) for _ in range(num_points)]
tx2 = [random.randint(-32, 31) for _ in range(num_points)]
tx3 = [random.randint(-32, 31) for _ in range(num_points)]

# Write to CSV file
with open('random_signals .csv', 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['remote', 'tx0', 'tx1', 'tx2', 'tx3'])
    for i in range(num_points):
        writer.writerow([remote[i], tx0[i], tx1[i], tx2[i], tx3[i]])
