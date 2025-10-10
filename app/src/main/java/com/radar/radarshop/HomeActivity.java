package com.radar.radarshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView avatarTv;
    private TextView userNameTv;
    private TextView welcomeTv;

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            EdgeToEdge.enable(this);
        } catch (Throwable ignored) {
            // EdgeToEdge is nice-to-have; ignore on older setups
        }

        try {
            setContentView(R.layout.activity_home);

            // Session guard — if not logged in, bounce to Auth
            session = new SessionManager(this);
            if (!session.isLoggedIn()) {
                startActivity(new Intent(this, AuthActivity.class));
                finish();
                return;
            }

            initializeViews();
            setupUserInfo();
            setupClickListeners();
            setupBottomNavigation();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading home screen", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void initializeViews() {
        avatarTv = findViewById(R.id.avatar);
        userNameTv = findViewById(R.id.userName);
        welcomeTv = findViewById(R.id.welcome);
        bottomNav = findViewById(R.id.bottomNav);

        // Ensure critical views exist (match activity_home.xml)
        if (avatarTv == null || userNameTv == null || bottomNav == null) {
            throw new RuntimeException("Required views not found in activity_home layout");
        }
    }

    private void setupUserInfo() {
        String email = session.getEmail();
        String fullName = "";  // will fall back to "Welcome" if empty

        db = new DatabaseHelper(this);
        try {
            if (email != null && !email.trim().isEmpty()) {
                DatabaseHelper.UserProfile profile = db.getUserProfile(email);
                if (profile != null) {
                    String dbName = safe(profile.fullName());
                    if (!dbName.isEmpty()) fullName = dbName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // continue with defaults
        }

        // Bind UI (fallbacks)
        userNameTv.setText(fullName.isEmpty() ? "Welcome" : fullName);
        avatarTv.setText(computeInitials(fullName, email));

        if (welcomeTv != null) {
            welcomeTv.setText("Welcome back,");
        }
    }

    private void setupClickListeners() {
        // Open Profile on avatar tap
        avatarTv.setOnClickListener(v -> openProfile());

        // Also open Profile when the whole header row is tapped (optional UX)
        View welcomeRow = findViewById(R.id.headerUserRow);
        if (welcomeRow != null) {
            welcomeRow.setOnClickListener(v -> openProfile());
        }
    }

    private void setupBottomNavigation() {
        try {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    // Already on Home
                    return true;
                } else if (id == R.id.nav_shop) {
                    openPlaceholder("Shop");
                    return true;
                } else if (id == R.id.nav_orders) {
                    openPlaceholder("Orders");
                    return true;
                } else if (id == R.id.nav_cart) {
                    openPlaceholder("Cart");
                    return true;
                } else if (id == R.id.nav_wishlist) {
                    openPlaceholder("Wishlist");
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Navigation setup failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void openProfile() {
        try {
            startActivity(new Intent(this, ProfileActivity.class));
        } catch (Throwable t) {
            Toast.makeText(this, "Cannot open profile", Toast.LENGTH_SHORT).show();
        }
    }


    private void openPlaceholder(String screenName) {
        Toast.makeText(this, screenName + " — Coming soon", Toast.LENGTH_SHORT).show();
        // When you add real screens, navigate here.
        /*
        try {
            Class<?> activityClass = Class.forName("com.radar.radarshop." + screenName + "Activity");
            Intent intent = new Intent(this, activityClass);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, screenName + " screen not available", Toast.LENGTH_SHORT).show();
        }
        */
    }

    /** Make initials from full name, else from email user part, else "U". */
    @NonNull
    private String computeInitials(@Nullable String fullName, @Nullable String email) {
        // Try full name
        if (fullName != null) {
            String t = fullName.trim();
            if (!t.isEmpty()) {
                String[] parts = t.split("\\s+");
                if (parts.length >= 2) {
                    return "" + up(parts[0]) + up(parts[1]);
                } else if (parts.length == 1 && parts[0].length() > 0) {
                    return "" + Character.toUpperCase(parts[0].charAt(0));
                }
            }
        }
        // Fallback to email (before @)
        if (email != null && !email.isEmpty()) {
            String[] pieces = email.split("@", 2);
            String user = pieces.length > 0 ? pieces[0] : "";
            if (!user.isEmpty()) {
                char c1 = Character.toUpperCase(user.charAt(0));
                char c2 = user.length() > 1 ? Character.toUpperCase(user.charAt(1)) : 0;
                return c2 == 0 ? String.valueOf(c1) : ("" + c1 + c2);
            }
        }
        // Final fallback
        return "U";
    }

    private char up(@NonNull String s) {
        return Character.toUpperCase(s.charAt(0));
    }

    @NonNull
    private String safe(@Nullable String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}
