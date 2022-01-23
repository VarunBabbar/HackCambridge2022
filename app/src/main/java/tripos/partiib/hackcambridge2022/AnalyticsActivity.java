package tripos.partiib.hackcambridge2022;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class AnalyticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        ImageView pieChart = findViewById(R.id.pieChart);
        ImageView trend = findViewById(R.id.trend);
    }
}