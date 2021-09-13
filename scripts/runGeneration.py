import os, time

for test_number in range(100):
    print("Running Test No {}".format(test_number))

    os.system("time java -cp gin.jar gin.util.GPMemory -d . -p gson -x 2000  "
          "-m create_profile_results.csv -h /usr/local/Cellar/maven/3.6.3_1/libexec -in 21 -gn 1 -r 500 "
          "-o samples2/sampler_results{}.csv".format(test_number))
