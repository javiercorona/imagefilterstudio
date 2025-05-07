# ImageFilterStudio

A simple Android application to apply image filters using Chaquopy-powered Python scripts.

## Features

* Pick an existing image from the gallery
* Apply various Python-defined filters (via Chaquopy)
* Save the filtered image to the device gallery
* **New:** Capture a photo directly with the device camera

## Getting Started

### Prerequisites

* Android Studio Bumblebee or later
* Android SDK (API Level 35 or higher)
* Kotlin 1.9.22+
* Java 17
* Chaquopy plugin for Python support

### Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-username/ImageFilterStudio.git
   cd ImageFilterStudio
   ```

2. **Open in Android Studio**

   * Launch Android Studio
   * Choose **Open an existing project**
   * Select the cloned `ImageFilterStudio` folder

3. **Sync Gradle**

   * Let Android Studio download all dependencies, including Chaquopy
   * Ensure the Python component (`filters.py`) under `app/src/main/python/` is intact

4. **Configure Python dependencies** (if adding new filters)

   * Edit `filters.py` to define Python functions for your filters
   * Add any required Python packages in `app/build.gradle` under `chaquopy { pip { install ... } }`

## Usage

1. **Run the app** on a physical device or emulator with camera support.
2. **Pick Image**: Tap **Pick Image** to select an existing photo.
3. **Apply Filter**: Choose a filter from the spinner and tap **Apply Filter**.
4. **Save**: Tap **Save** to store the filtered image in your gallery.
5. **Camera**: Tap **Camera** to launch the device camera, take a photo, and preview it.

## Project Structure

```
ImageFilterStudio/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/imagefilterstudio/  ← Kotlin source files
│   │   │   ├── res/layout/                         ← XML layouts (`activity_main.xml`)
│   │   │   ├── python/                             ← Python filter scripts (`filters.py`)
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   └── build.gradle
└── settings.gradle
```

## Contributing

1. Fork the repository.
2. Create a new branch: `git checkout -b feature/YourFeature`.
3. Commit your changes: `git commit -m "Add your feature"`.
4. Push to the branch: `git push origin feature/YourFeature`.
5. Open a Pull Request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
