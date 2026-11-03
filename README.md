# A4: Concurrency

## Setup and Usage

Requires Java JDK 17 or newer.

Put your JPG image in the `input` folder.

Using a separate `build` folder is suggested because it keeps compiled files separate from the source code and keeps the project clean.

Run these commands from the project root folder:

```bash
mkdir -p build
javac --release 17 -d build ImageAverager.java
java -cp build ImageAverager istanbul.jpg 20 S
```

To process regions in parallel:

```bash
java -cp build ImageAverager istanbul.jpg 20 M
```

On Windows Command Prompt, use `mkdir build` instead of `mkdir -p build`.

Replace `istanbul.jpg` with your filename and `20` with the square size. Use `S` for single-threaded mode or `M` for multithreaded mode.

## Image Display

Large images fit inside the screen when the window opens. Resizing the window keeps the image centered and preserves its proportions. Only the preview is scaled; averaging uses the original image resolution.

The window first shows the original image, then displays the averaging progress. Wait for the window title to show "Complete" before closing it.

## Square Averaging

`averageSquare` calculates the average red, green, and blue values of one square, then fills that square with the average color. Division uses whole numbers, so fractional values are rounded down. Squares at the image edges use only the pixels inside the image. Both processing modes use this method.

## Single-Threaded Processing

One processing thread handles squares from left to right, then top to bottom. Swing uses a separate thread for the window, so it can still be resized during processing. A short pause after each row makes progress visible.

## Multithreaded Processing

`Runtime.getRuntime().availableProcessors()` provides the available CPU core count. The program uses that many workers, or fewer if there are fewer rows of squares. Each worker processes a separate horizontal region, from left to right and top to bottom. Regions start at square boundaries.

Workers average private copies of their rows, so calculations run in parallel. Short `synchronized` blocks protect copying pixels to and from the shared image. Drawing uses the same lock. The program calls `join()` on every worker before saving. A worker error prevents saving an incomplete result.

Both modes pause for 20 milliseconds after each row to make progress visible. These pauses are for visualization, so total runtime includes an artificial delay.

## Saving the Result

After processing finishes, the program saves the averaged image as `result.jpg` in the project root folder. It keeps the original dimensions, regardless of the preview size. Each successful run replaces the previous result. Wait for completion before closing the window so the new result can be saved.

After saving successfully, it prints:

```text
Saved image to result.jpg
Processing complete.
```

## Successful Runs

Both modes completed with `istanbul.jpg` at 2240 x 1260 pixels and square size 20.

Single-threaded mode:

```text
java -cp build ImageAverager istanbul.jpg 20 S
Image size: 2240 x 1260
Square size: 20
Mode: S
Saved image to result.jpg
Processing complete.
```

Multithreaded mode:

```text
java -cp build ImageAverager istanbul.jpg 20 M
Image size: 2240 x 1260
Square size: 20
Mode: M
Available CPU cores: 10
Processing threads: 10
Saved image to result.jpg
Processing complete.
```

The CPU core and thread counts depend on the computer and the number of square rows in the image.

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
