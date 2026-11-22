package com.portsnativos111.oot3dport;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    private static final int PICK_ROM_CODE = 101;
    private static final int PICK_PATCH_CODE = 102;

    private boolean hasRom = false;
    private boolean hasPatch = false;

    private TextView txtStatus;
    private Button btnLaunch;

    static {
        System.loadLibrary("oot3d_native");
    }

    public native void startNativeEngine(String internalFilesPath);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSelectRom = findViewById(R.id.btnSelectRom);
        Button btnSelectPatch = findViewById(R.id.btnSelectPatch);
        btnLaunch = findViewById(R.id.btnLaunch);
        txtStatus = findViewById(R.id.txtStatus);

        checkExistingFiles();

        btnSelectRom.setOnClickListener(v -> openFilePicker(PICK_ROM_CODE));
        btnSelectPatch.setOnClickListener(v -> openFilePicker(PICK_PATCH_CODE));

        btnLaunch.setOnClickListener(v -> {
            txtStatus.setText("Iniciando motor nativo ARM64...");
            startNativeEngine(getFilesDir().getAbsolutePath());
        });
    }

    private void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (requestCode == PICK_ROM_CODE) {
                if (copyFileToInternal(uri, "oot3d_base.rom")) {
                    hasRom = true;
                    Toast.makeText(this, "ROM cargada correctamente", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == PICK_PATCH_CODE) {
                if (copyFileToInternal(uri, "SingleScreen_3.2.2.zip")) {
                    hasPatch = true;
                    Toast.makeText(this, "SingleScreen 3.2.2 cargado", Toast.LENGTH_SHORT).show();
                }
            }
            updateUI();
        }
    }

    private boolean copyFileToInternal(Uri uri, String destFileName) {
        try (InputStream in = getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(new File(getFilesDir(), destFileName))) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            return true;
        } catch (Exception e) {
            txtStatus.setText("Error al copiar archivo: " + e.getMessage());
            return false;
        }
    }

    private void checkExistingFiles() {
        File romFile = new File(getFilesDir(), "oot3d_base.rom");
        File patchFile = new File(getFilesDir(), "SingleScreen_3.2.2.zip");
        hasRom = romFile.exists() && romFile.length() > 0;
        hasPatch = patchFile.exists() && patchFile.length() > 0;
        updateUI();
    }

    private void updateUI() {
        if (hasRom && hasPatch) {
            txtStatus.setText("Estado: ¡Listo para jugar!");
            btnLaunch.setEnabled(true);
        } else if (hasRom) {
            txtStatus.setText("Estado: Falta el archivo SingleScreen 3.2.2");
        } else if (hasPatch) {
            txtStatus.setText("Estado: Falta la ROM de OoT 3DS");
        }
    }
}
