mvn clean package
if [ $? -eq 0 ]; then
    rm -rf ../flink-1.14.0/examples/streaming/dairypipelinej-0.1.jar ;cp target/dairypipelinej-0.1.jar  ../flink-1.14.0/examples/streaming/
    echo "INSTALLED."
fi
