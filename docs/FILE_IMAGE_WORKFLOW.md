# Product Image Workflow (Local Storage First)

This guide describes the new image workflow from local files to product binding.

## 1) Upload images from local machine (multipart)

Endpoint: `POST /api/files/upload/multiple`

- form-data key: `files` (repeat for multi-files)
- optional params:
  - `category`: e.g. `spu-1002`
  - `generateThumbnail`: `true|false`
  - `addWatermark`: `true|false`

## 2) Upload all files from a server-side local folder

Endpoint: `POST /api/files/upload/local-folder`

- params:
  - `folderPath` (required): absolute path on backend machine
  - `recursive` (optional): default `false`
  - `category` / `generateThumbnail` / `addWatermark` same as above

## 3) Bind uploaded URLs to SPU/SKU

Endpoint: `PUT /api/products/{spuId}/images`

Example payload:

```json
{
  "mainImageUrl": "/cdn/images/AirPods Pro 2.jpg",
  "images": [
    {
      "imageType": 1,
      "imageUrl": "/cdn/images/AirPods Pro 2.jpg",
      "sortOrder": 0
    },
    {
      "imageType": 2,
      "imageUrl": "/cdn/images/AirPods Pro 2-detail.jpg",
      "sortOrder": 1
    },
    {
      "imageType": 3,
      "skuId": 5001,
      "imageUrl": "/cdn/images/AirPods Pro 2-sku.jpg",
      "sortOrder": 0
    }
  ]
}
```

`imageType` meanings:
- `1`: SPU main image
- `2`: SPU detail/gallery image
- `3`: SKU image (requires `skuId`)

## 4) Database write behavior

- `product_spu.main_image` <- `mainImageUrl` (or first `imageType=1`)
- `product_sku.image_url` <- first image per SKU in payload
- `product_image` <- replaced by request image list for this SPU

## 5) Frontend rendering

- Product cards use `product_spu.main_image`
- Product detail uses `main_image + product_image + sku.image_url`
- URL resolver strips quotes and normalizes `/cdn/images/...`

