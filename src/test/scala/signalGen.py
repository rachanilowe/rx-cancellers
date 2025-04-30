# signals_gen.py
import csv
import random

with open('test_signals.csv', 'w') as f:
    writer = csv.writer(f)
    for _ in range(500):  # 500 test steps
        remote = random.randint(-100, 100)
        noises = [random.randint(-32, 31) for _ in range(4)]
        received = remote + (noises[0] >> 3) + (noises[1] >> 4) + (noises[2] >> 4) + (noises[3] >> 4) 
        # received = remote + sum(n >> 3 for n in noises)
        writer.writerow([remote] + noises + [received])
