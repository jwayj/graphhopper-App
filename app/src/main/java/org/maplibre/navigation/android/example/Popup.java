package org.maplibre.navigation.android.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Popup extends AppCompatActivity {

    private EditText editStart;
    private EditText editDestination;
    private EditText editDistance; // 거리 입력 EditText 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        // 닫기 버튼
        ImageView closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finish());

        // 출발지 입력창 클릭 시 검색 팝업 띄우기
        editStart = findViewById(R.id.editStart);
        editStart.setOnClickListener(v -> {
            Intent intent = new Intent(Popup.this, SearchPopup.class);
            startActivityForResult(intent, 1); // 검색 팝업 실행
            // 키보드가 뜨지 않도록 하기 위해 EditText 포커스 제거
            editStart.clearFocus();
        });

        // 도착지 입력창 클릭 시 검색 팝업 띄우기
        editDestination = findViewById(R.id.editDestination);
        editDestination.setOnClickListener(v -> {
            Intent intent = new Intent(Popup.this, SearchPopup.class);
            startActivityForResult(intent, 2); // 검색 팝업 실행
            // 키보드가 뜨지 않도록 하기 위해 EditText 포커스 제거
            editDestination.clearFocus();
        });

        // 거리 입력
        editDistance = findViewById(R.id.editDistance); // 거리 입력칸
        editDistance.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 엔터 키가 눌렸을 때, 입력된 거리 값 저장
                String distance = editDistance.getText().toString();

                // 여기서 값을 처리하는 로직을 추가할 수 있음 (예: 서버로 전송)
                // 예시: Log.d("Distance", "입력된 거리: " + distance);

                // 키보드를 닫기
                hideKeyboard(v);
                return true;
            }
            return false;
        });
    }

    // 키보드 숨기기
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void onSearchRouteClicked(View view) {
        String start = editStart.getText().toString();
        String destination = editDestination.getText().toString();
        String distanceStr = editDistance.getText().toString();

        // 거리 값 숫자 변환 검증
        try {
            double desiredDistance = Double.parseDouble(distanceStr);

            Intent intent = new Intent(Popup.this, MockNavigationActivity.class);
            intent.putExtra("start_address", start);
            intent.putExtra("end_address", destination);
            intent.putExtra("desired_distance", desiredDistance);

            startActivity(intent); // 새 액티비티 시작
            finish(); // 현재 팝업 닫기

        } catch (NumberFormatException e) {
            editDistance.setError("유효한 숫자를 입력하세요");
            editDistance.requestFocus();
        }
    }


    // 주소 선택 후 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String selectedAddress = data.getStringExtra("selectedAddress");

            // 출발지 또는 도착지에 주소 입력하기
            if (requestCode == 1) {
                editStart.setText(selectedAddress); // 출발지 입력칸에 주소 반영
            } else if (requestCode == 2) {
                editDestination.setText(selectedAddress); // 도착지 입력칸에 주소 반영
            }
        }
    }
}