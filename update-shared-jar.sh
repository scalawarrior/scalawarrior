#!/bin/sh
./activator clean compile
(cd ./shared/.js/target/scala-2.11/classes;jar -cvf scala-warrior.jar *)
mv ./shared/.js/target/scala-2.11/classes/scala-warrior.jar ./play/conf/compiler/
