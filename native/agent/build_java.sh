cd java
rm -f -r classes
mkdir -p classes
javac -d classes Mtrace.java
(cd classes; jar -cf ../../mtrace.jar Mtrace.class)
cd ..