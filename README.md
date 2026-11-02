# A4: Concurrency

## Setup and Usage

Requires Java JDK 17 or newer.

Put your JPG image in the `input` folder.

Using a separate `build` folder is suggested because it keeps compiled files separate from the source code and keeps the project clean.

Run these commands from the `A4` folder:

```bash
mkdir -p build
javac --release 17 -d build ImageAverager.java
java -cp build ImageAverager istanbul.jpg 20 S
```

On Windows Command Prompt, use `mkdir build` instead of `mkdir -p build`.

Replace `istanbul.jpg` with your filename and `20` with the square size. Use `S` for single-threaded mode or `M` for multithreaded mode.

## Request Validation

The program checks the argument count, square size, processing mode, and input file.

Missing arguments:

```text
java -cp build ImageAverager
Exactly 3 arguments are required: image filename, square size, and processing mode.
Usage: java -cp build ImageAverager <file.jpg> <square size> <S|M>
```

Zero, negative, and non-number square sizes:

```text
java -cp build ImageAverager istanbul.jpg 0 S
Square size must be a positive whole number.

java -cp build ImageAverager istanbul.jpg -5 S
Square size must be a positive whole number.

java -cp build ImageAverager istanbul.jpg abc S
Square size must be a positive whole number.
```

Invalid processing mode:

```text
java -cp build ImageAverager istanbul.jpg 20 X
Processing mode must be S or M.
```

Missing file and wrong file extension:

```text
java -cp build ImageAverager missing.jpg 20 S
Could not open image: input/missing.jpg

java -cp build ImageAverager photo.png 20 S
Please provide a JPG image (.jpg or .jpeg).
```
