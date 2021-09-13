import csv, operator, statistics

li = []
li2 = []
top_fitness_list = []
top_memory_usage_list = []


def fitness_improvement(sorted_fitness_list):
    for item in sorted_fitness_list:
        li.append(item[6])
    li.remove('FitnessImprovement')

    samples = []
    for item in li:
        samples.append(float(item))

    samples = sorted(samples, reverse=True)
    top_fitness_list.append(samples[0])


def memory_usage(sorted_fitness_list):
    top_value = 0
    for item in sorted_fitness_list:
        top_value += 1

        if top_value == 2:
            li2.append(item[7])
            break

    samples = []
    for item in li2:
        samples.append(float(item))

    samples = sorted(samples)
    top_memory_usage_list.append(samples[0])


for test_number in range(100):
    import csv

    with open('samples/sampler_results{}.csv'.format(test_number), 'r') as file:
        reader = csv.reader(file)

        # Sort by FitnessImprovement
        sorted_fitness_list = sorted(reader, key=operator.itemgetter(6), reverse=True)
        fitness_improvement(sorted_fitness_list)

        memory_usage(sorted_fitness_list)

print("")
print("FitnessImprovement Variance {:.3f}".format((statistics.variance(top_fitness_list))))
print("MemoryUsed (MB) Variance {:.3f} MB".format((statistics.variance(top_memory_usage_list))))
