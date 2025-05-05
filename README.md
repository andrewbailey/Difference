# Difference
Difference is a Kotlin multiplatform differencing library.
Given two lists, Difference will compute the insert and delete operations required to transform the starting list into the final list.
Difference can also optionally detect items that have moved to new indices in the list.

Behind the scenes, Difference uses Eugene Myer's Differencing Algorithm.
This is the same algorithm used by Android's [DiffUtil](https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil) class, which you may be familiar with if you're an Android developer.
Difference is very similar to Android's DiffUtil, but is completely platform agnostic and has more receiver-agnostic APIs to consume the diff result.

## Setup
Gradle can automatically resolve which variation of Difference is appropriate for the platform and architecture you're compiling for.
To use the universal dependency, add this dependency to your module's `build.gradle`:

```groovy
dependencies {
    implementation 'dev.andrewbailey.difference:difference:1.1.1'
}
```

If you want to explicitly specify which platform variant of the library you want to depend on, you can use any of the following dependencies as appropriate:

```
dev.andrewbailey.difference:difference-jvm:1.1.1
dev.andrewbailey.difference:difference-js:1.1.1
dev.andrewbailey.difference:difference-linux-x64:1.1.1
dev.andrewbailey.difference:difference-macos-x64:1.1.1
dev.andrewbailey.difference:difference-ios-x64:1.1.1
dev.andrewbailey.difference:difference-ios-arm64:1.1.1
dev.andrewbailey.difference:difference-ios-simulator-arm64:1.1.1
dev.andrewbailey.difference:difference-mingw-x64:1.1.1
```

## Generating a diff
To generate a diff, you can call `differenceOf` with the following arguments:

```kotlin
val listOfBffs = listOf("Merengue", "Sherb", "Tammy")
val revisedListOfBffs = listOf("Merengue", "Sprinkle", "Sherb")

val diff = differenceOf(
    original = listOfBffs,
    updated = revisedListOfBffs,
    detectMoves = false
)
```

In Java, you can write this code as:

```java
List<String> listOfBffs = Arrays.asList("Merengue", "Sherb", "Tammy");
List<String> revisedListOfBffs = Arrays.asList("Merengue", "Sprinkle", "Sherb");

DiffResult<String> diff = Difference.differenceOf(listOfBffs, revisedListOfBffs, false);
```

The diff that will be generated for this input will look like this:
```
Insert(index = 1, item = "Sprinkle")
Remove(index = 3)
```

Please keep in mind that calling `differenceOf` is an expensive operation, so it's recommended to offload this call to a background thread.
See the [Performance](#performance) section for more info.

## Using a `DiffResult`

`differenceOf` returns a `DiffResult<T>` object, typed based on the input lists.
To consume a diff, you can call `DiffResult<T>.applyDiff`.
There are several overloads that can increase the performance of how your diff is applied, but the most basic call looks something like this:

```kotlin
...

val diff = differenceOf(
    original = listOfBffs,
    updated = revisedListOfBffs,
    detectMoves = true
)

// When `applyDiff` returns, `output` will be equal to `revisedListOfBffs`
val output = listOfBffs.toMutableList()
diff.applyDiff(
    remove = { index: Int ->
        output.removeAt(index)
    },
    insert = { item: T, index: Int ->
        output.add(index, item)
    },
    move = { oldIndex: Int, newIndex: Int ->
        // You can leave this blank if you set `detectMoves` to false
        output.add(
            element = removeAt(oldIndex),
            index = if (newIndex < oldIndex) {
                newIndex
            } else {
                newIndex - 1
            }
        )
    }
)
```

If you're using Difference in Java, using `applyDiff` can be a bit awkward to use because it exposes Kotlin's `FunctionN` interfaces, named parameters, and default arguments to be easy to call.
You can alternatively use `DiffReceiver` to improve the readability of your diff callbacks in a Java project.

The same receiver logic can be expressed like this in Java:

```java
DiffResult<String> diff = differenceOf(...);

// When `applyDiff` returns, `output` will be equal to `revisedListOfBffs`
List<String> output = ArrayList<>(listOfBffs);
DiffReceiver<String> receiver = new DiffReceiver<>() {
    @Override
    public void remove(int index) {
        output.removeAt(index);
    }

    @Override
    public void insert(String item, int index) {
        output.add(index, item);
    }

    @Override
    public void move(int oldIndex, int newIndex) {
        // You can omit this override if you set `detectMoves` to false
        int index = newIndex;
        if (newIndex >= oldIndex) {
            index--;
        }

        output.add(index, output.removeAt(oldIndex));
    }
};

receiver.applyDiff(diff);
```

Note that `DiffReceiver` is only available in Java projects.
If your project is a 100% Kotlin project, then you should stick to `applyDiff` and ignore `DiffReceiver`.

## Performance
Difference uses a linear-space non-recursive implementation of Eugene Myer's Differencing algorithm.
The algorithm takes O((M+N)×D + D log D) operations, where M and N are the lengths of the input lists, and D is the minimum number of edits it takes to transform the original list into the final list.
If you enable movement detection, this runtime increases by O(D²).

Diff generation can take a while, so for UI-centric applications, you should avoid calling `differenceOf` on your main thread.
Depending on conditions and the client's hardware, diff generation for arbitrary lists can easily cause your application to block the main thread and cause frame drops if you aren't careful.

### Benchmarks

Difference has benchmark tests that can run on an Android device.
Below is the measured time it takes to compute diffs of various sizes on a Pixel 3 running Android Q.

These values **should not** be used to estimate how long any call to `differenceOf` will take.
Real-world performance will vary device-to-device, and can be influenced by conditions such as the device's hardware, CPU load, temperature, and battery level, among other factors.
You should take these values with a grain of salt and make note of how the time taken to generate a diff grows exponentially based on the size of the inputs.

#### Pixel 3 Benchmark (without movement detection)
| Starting list size | Number of operations | Time taken |
|:------------------:|:--------------------:|:-----------|
| 100                | 10                   | 0.059 ms   |
| 1000               | 100                  | 0.983 ms   |
| 5000               | 500                  | 17.6 ms    |
| 10000              | 1000                 | 55.6 ms    |

#### Pixel 3 Benchmark (with movement detection)
| Starting list size | Number of operations | Time taken |
|:------------------:|:--------------------:|:-----------|
| 100                | 10                   | 0.055 ms   |
| 1000               | 100                  | 1.158 ms   |
| 5000               | 500                  | 22.0 ms    |
| 10000              | 1000                 | 73.5 ms    |
