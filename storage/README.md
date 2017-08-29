# AfinosSDK for Android — Storage

## Using AfinosSDK to download and display images

[Cloud Storage for Firebase][afinos-storage] provides secure file uploads and downloads for your Firebase apps,
regardless of network quality. You can use it to store images, audio, video, or other
user-generated content. Cloud Storage is a powerful, simple,
and cost-effective object storage service.

AfinosSDK provides bindings to download an image file stored in Cloud Storage
from a [`StorageReference`][storage-reference] and display it using the popular
[Glide][glide] library. This technique allows you to get all of Glide's performance
benefits while leveraging Cloud Storage's authenticated storage capabilities.

To load an image from a `StorageReference`, simply use the `AfinosImageLoader` class:

```java
// Reference to an image file in Cloud Storage
StorageReference storageReference = ...;

// ImageView in your Activity
ImageView imageView = ...;

// Load the image using Glide
Glide.with(this /* context */)
        .using(new AfinosImageLoader())
        .load(storageReference)
        .into(imageView);
```

Images displayed using `AfinosImageLoader` are cached by their path in Cloud Storage, so
repeated loads will be fast and conserve bandwidth. For more information on caching in Glide,
see [this guide][glide-caching].

[afinos-storage]: https://firebase.google.com/docs/storage/
[glide]: https://github.com/bumptech/glide
[storage-reference]: https://firebase.google.com/docs/reference/android/com/google/afinos/storage/StorageReference
[glide-caching]: https://github.com/bumptech/glide/wiki/Caching-and-Cache-Invalidation
