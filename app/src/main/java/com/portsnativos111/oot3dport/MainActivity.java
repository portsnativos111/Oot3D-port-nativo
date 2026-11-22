package com.portsnativos111.oot3dport;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;

public class MainActivity extends Activity {

    private static final int PICK_ROM_CODE = 101;
    private static final int PICK_PATCH_CODE = 102;

    private static final String PREFS_NAME = "oot3d_prefs";
    private static final String KEY_ROM_URI = "rom_uri";
    private static final String KEY_PATCH_URI = "patch_uri";

    private Uri romUri = null;
    private Uri patchUri = null;

    private TextView txtStatus;
    private Button btnLaunch;

    static {
        System.loadLibrary("oot3d_native");
    }

    public native void startNativeEngine(String romUriStr, String patchUriStr);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSelectRom = findViewById(R.id.btnSelectRom);
        Button btnSelectPatch = findViewById(R.id.btnSelectPatch);
        btnLaunch = findViewById(R.id.btnLaunch);
        txtStatus = findViewById(R.id.txtStatus);

        loadSavedUris();

        btnSelectRom.setOnClickListener(v -> openFilePicker(PICK_ROM_CODE));
        btnSelectPatch.setOnClickListener(v -> openFilePicker(PICK_PATCH_CODE));

        btnLaunch.setOnClickListener(v -> {
            if (isUriAccessible(romUri) && isUriAccessible(patchUri)) {
                txtStatus.setText("Iniciando motor nativo ARM64...");
                startNativeEngine(romUri.toString(), patchUri.toString());
            } else {
                Toast.makeText(this, "Vuelve a seleccionar ROM y mod", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
    }

    private void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                if (takeFlags != 0) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (SecurityException ignoredAgain) {
                    }
                }
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            if (requestCode == PICK_ROM_CODE) {
                romUri = uri;
                prefs.edit().putString(KEY_ROM_URI, uri.toString()).apply();
                Toast.makeText(this, "ROM guardada para futuros inicios", Toast.LENGTH_SHORT).show();
            } else if (requestCode == PICK_PATCH_CODE) {
                patchUri = uri;
                prefs.edit().putString(KEY_PATCH_URI, uri.toString()).apply();
                Toast.makeText(this, "SingleScreen 3.2.2 guardado", Toast.LENGTH_SHORT).show();
            }
            updateUI();
        }
    }

    private void loadSavedUris() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedRom = prefs.getString(KEY_ROM_URI, null);
        String savedPatch = prefs.getString(KEY_PATCH_URI, null);

        if (savedRom != null) {
            Uri parsed = Uri.parse(savedRom);
            if (isUriAccessible(parsed)) {
                romUri = parsed;
            }
        }
        if (savedPatch != null) {
            Uri parsed = Uri.parse(savedPatch);
            if (isUriAccessible(parsed)) {
                patchUri = parsed;
            }
        }
        updateUI();
    }

    private boolean isUriAccessible(Uri uri) {
        if (uri == null) return false;
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            return is != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateUI() {
        boolean romOk = isUriAccessible(romUri);
        boolean patchOk = isUriAccessible(patchUri);

        if (romOk && patchOk) {
            txtStatus.setText("Estado: ¡ROM y SingleScreen 3.2.2 listos!");
            btnLaunch.setEnabled(true);
        } else if (romOk) {
            txtStatus.setText("Estado: Falta ubicar SingleScreen 3.2.2");
            btnLaunch.setEnabled(false);
        } else if (patchOk) {
            txtStatus.setText("Estado: Falta ubicar la ROM del juego");
            btnLaunch.setEnabled(false);
        } else {
            txtStatus.setText("Estado: Falta ubicar ROM y mod");
            btnLaunch.setEnabled(false);
        }
    }
}
