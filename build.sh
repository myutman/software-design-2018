cd term2
for lab in $(ls | grep "lab"); do
  cd $lab
  ./gradlew build
  cd ..
done
