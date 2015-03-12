cd java
rm -f -r classes
mkdir -p classes
javac -d classes Agent.java
(cd classes; jar -cf ../../agent.jar Agent.class)
cd ..