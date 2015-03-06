rm -f -r classes
mkdir -p classes
javac -d classes Mtrace.java
(cd classes; jar -cf ../../build/mtrace.jar Mtrace.class)