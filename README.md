# Clay

Clay is an Android library project that provides image trimming. Fully written in Kotlin, Clay is originally a UI component of [LINE Creators Studio](https://creator.line.me/en/studio/), a LINE app for creating LINE stickers. You can use Clay with any Android application to trim the outline of an image and create your own custom shape.

* Produce a trimmed image
* Zoom in/out an image
* Undo previous trimming actions

![](https://github.com/line/clay/blob/master/screenshot/screenshot.png)

Click [here](https://github.com/line/clay/blob/master/screenshot/example.gif) to see how you can trim an image into a random shape.

## How to build

Use Gradle to build the library. Download it from [JCenter](https://bintray.com/bintray/jcenter) and add configurations in the build.gradle file as follows.

```
allprojects {
    repositories {
        jcenter()
    }
}

dependencies {
  compile 'com.linecorp:clayview:0.1.2'
}
```

## Getting started

To get started with Clay, define its view in an XML layout file as follows.

```
  <com.linecorp.clay.view.ClayView
    android:id="@+id/clay"
    android:src="@drawable/test2"
    app:strokeWidth="6dp"
    app:endPointRadius="20dp"
    app:controlPointColor="@android:color/holo_red_dark"
    app:strokeColor="@color/orange_ffcc80" />
```

Then you can use ClayView as follows.

```
val clayView = findViewById(R.id.clay) as ClayView

clayView.undoSelect() // undo
clayView.getCroppedImage() // get the cropped image

```

You can find a more advanced way of using the library from the [example](https://github.com/line/clay/tree/master/app).

## How to contribute to Clay

See [CONTRIBUTE.md](CONTRIBUTE.md)

## License

```
Copyright 2017 LINE Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
