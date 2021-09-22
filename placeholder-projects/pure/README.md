# Build process

From the context of this project directory run
(replaing `/some/destination/dir/` at first to your actual dir)

```
./gradlew clean assemble &&
cp build/distributions/script.js /some/destination/dir/ &&
cp build/distributions/script.js.map /some/destination/dir/
```

