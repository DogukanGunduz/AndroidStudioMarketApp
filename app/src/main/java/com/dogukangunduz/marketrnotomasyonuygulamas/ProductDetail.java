package com.dogukangunduz.marketrnotomasyonuygulamas;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.dogukangunduz.marketrnotomasyonuygulamas.databinding.ActivityProductDetailBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ProductDetail extends AppCompatActivity {
    public ActivityProductDetailBinding binding;

    ActivityResultLauncher<Intent> galleryLauncher;
    ActivityResultLauncher<String> permissionLauncher;

    Bitmap selectedImage;

    int id;
    String reason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Intent intent = getIntent();
        reason = intent.getStringExtra("reason");
        id = intent.getIntExtra("id", -1);
        if (reason != null && reason.matches("showProduct")) {
            try {
                SQLiteDatabase db = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
                Cursor cursor = db.rawQuery("SELECT * FROM products WHERE id = " + id, null);
                int idX = cursor.getColumnIndex("id");
                int nameX = cursor.getColumnIndex("productName");
                int priceX = cursor.getColumnIndex("productPrice");
                int stocX = cursor.getColumnIndex("productStock");
                int imageX = cursor.getColumnIndex("image");
                while (cursor.moveToNext()) {
                    binding.productNameText.setText(cursor.getString(nameX));
                    binding.productPriceText.setText(String.valueOf(cursor.getInt(priceX)));
                    binding.productStockText.setText(String.valueOf(cursor.getInt(stocX)));

                    byte[] imageArray = cursor.getBlob(imageX);
                    Bitmap incImage = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
                    binding.image.setImageBitmap(incImage);
                }
                binding.button2.setVisibility(View.INVISIBLE);
                binding.button4.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Toast.makeText(ProductDetail.this, "Database failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            addProduct();
            binding.button2.setVisibility(View.VISIBLE);
        }

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == RESULT_OK) {
                    Intent data = o.getData();
                    if (data != null) {
                        Uri imageData = data.getData();
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.image.setImageBitmap(selectedImage);
                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageData);
                                binding.image.setImageBitmap(selectedImage);
                            }
                        } catch (Exception e) {
                            Toast.makeText(ProductDetail.this, "Failed to select image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if (o == true) {
                    Intent goToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(goToGallery);
                } else {
                    Toast.makeText(ProductDetail.this, "Permission Needed", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void selectImage(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(view, "Galeri İzni Vermeniz Gerekmektedir.", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                    }
                }).show();
            } else {
                Intent goToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(goToGallery);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(view, "Galeri İzni Vermeniz Gerekmektedir.", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                Intent goToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(goToGallery);
            }
        }
    }

    public Bitmap downscaleImage(Bitmap image, int maxValue) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scale = (float) width / (float) height;
        if (width > height) {
            width = maxValue;
            height = (int) (maxValue / scale);
        } else {
            height = maxValue;
            width = (int) (maxValue * scale);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void saveProduct(View view) {
        String productName = binding.productNameText.getText().toString();
        int productPrice = Integer.parseInt(binding.productPriceText.getText().toString());
        int productStock = Integer.parseInt(binding.productStockText.getText().toString());

        Bitmap miniImage = downscaleImage(selectedImage, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        miniImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        try {
            SQLiteDatabase db = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
            String sql = "INSERT INTO products (productName, productPrice, productStock,image) VALUES(?,?,?,?)";
            SQLiteStatement sqlStatus = db.compileStatement(sql);
            sqlStatus.bindString(1, productName);
            sqlStatus.bindLong(2, productPrice);
            sqlStatus.bindLong(3, productStock);
            sqlStatus.bindBlob(4, imageBytes);
            sqlStatus.execute();

            Intent goToProducts = new Intent(ProductDetail.this, Products.class);
            goToProducts.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(goToProducts);

            Toast.makeText(this, "Product saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ProductDetail.this, "Save product failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void addProduct() {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == RESULT_OK) {
                    Intent data = o.getData();
                    if (data != null) {
                        Uri imageData = data.getData();
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.image.setImageBitmap(selectedImage);
                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageData);
                                binding.image.setImageBitmap(selectedImage);
                            }
                        } catch (Exception e) {
                            Toast.makeText(ProductDetail.this, "Failed to select image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if (o == true) {
                    Intent goToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(goToGallery);
                } else {
                    Toast.makeText(ProductDetail.this, "Permission Needed", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    public void update(View view) {
        String productName = binding.productNameText.getText().toString();
        int productPrice = Integer.parseInt(binding.productPriceText.getText().toString());
        int productStock = Integer.parseInt(binding.productStockText.getText().toString());

        if (selectedImage != null) {
            Bitmap miniImage = downscaleImage(selectedImage, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            miniImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            try {
                SQLiteDatabase db = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
                String sql = "UPDATE products SET productName=?, productPrice=?, productStock=?, image=? WHERE id=?";
                SQLiteStatement sqlStatus = db.compileStatement(sql);
                sqlStatus.bindString(1, productName);
                sqlStatus.bindLong(2, productPrice);
                sqlStatus.bindLong(3, productStock);
                sqlStatus.bindBlob(4, imageBytes);
                sqlStatus.bindLong(5, id);
                sqlStatus.execute();
                Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(ProductDetail.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                SQLiteDatabase db = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
                String sql = "UPDATE products SET productName=?, productPrice=?, productStock=? WHERE id=?";
                SQLiteStatement sqlStatus = db.compileStatement(sql);
                sqlStatus.bindString(1, productName);
                sqlStatus.bindLong(2, productPrice);
                sqlStatus.bindLong(3, productStock);
                sqlStatus.bindLong(4, id);
                sqlStatus.execute();
                Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Toast.makeText(ProductDetail.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        }

        Intent goToProducts = new Intent(ProductDetail.this, Products.class);
        goToProducts.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goToProducts);
        finish();
    }

}