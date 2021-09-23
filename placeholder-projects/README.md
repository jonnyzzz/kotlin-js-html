# Build process

From the context of this project directory run (may omit defining env variables) the `stage` task.
By this design we need only single build logic while still
we need to have separate directories for different approaches.

For example for `pure` placeholder call:
```
export INPUT_FILE="$( pwd )/input-pure" &&
export OUTPUT_DIR="$( pwd )/" &&
./gradlew fullDistBuild
```
and for `react`:
```
export INPUT_FILE="$( pwd )/input-react" &&
export OUTPUT_DIR="$( pwd )/" &&
./gradlew fullDistBuild
```
and for `compose`:
```
export INPUT_FILE="$( pwd )/input-compose" &&
export OUTPUT_DIR="$( pwd )/" &&
./gradlew fullDistBuild
```

As a result you will get `.js` and `.js.map` files in the specified `OUTPUT_DIR` which will have
the same name as the placeholder project.

