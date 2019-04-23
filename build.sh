for lab in $(ls | grep "lab"); do
  cd $lab
  ./gradlew test
  cd ..
done
