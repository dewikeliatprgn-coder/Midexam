package com.example.midexam;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText editName, editId;
    CheckBox checkEnrolled;
    Button buttonAdd, buttonEdit, buttonDelete, buttonView;
    ListView listStudents;
    TextView textSummary, textEmpty;

    DatabaseHelper dbHelper;
    StudentAdapter adapter;
    ArrayList<String[]> rowList = new ArrayList<>(); // {id, name, enrolled}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // supaya isi tidak tertutup status bar / navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editName = findViewById(R.id.editName);
        editId = findViewById(R.id.editId);
        checkEnrolled = findViewById(R.id.checkEnrolled);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonView = findViewById(R.id.buttonView);
        listStudents = findViewById(R.id.listStudents);
        textSummary = findViewById(R.id.textSummary);
        textEmpty = findViewById(R.id.textEmpty);

        dbHelper = new DatabaseHelper(getApplicationContext());
        adapter = new StudentAdapter(this, rowList);
        listStudents.setAdapter(adapter);
        listStudents.setEmptyView(textEmpty); // tampil otomatis kalau data kosong

        // ADD
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editName.getText().toString().trim();
                String id = editId.getText().toString().trim();
                if (name.isEmpty() || id.isEmpty()) {
                    toast("Name and Student ID are required");
                    return;
                }
                if (dbHelper.addStudent(id, name, checkEnrolled.isChecked())) {
                    toast("Student added");
                    clearForm();
                    loadData();
                } else {
                    toast("Failed: Student ID already exists");
                }
            }
        });

        // EDIT
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editName.getText().toString().trim();
                String id = editId.getText().toString().trim();
                if (name.isEmpty() || id.isEmpty()) {
                    toast("Name and Student ID are required");
                    return;
                }
                if (dbHelper.updateStudent(id, name, checkEnrolled.isChecked())) {
                    toast("Student updated");
                    clearForm();
                    loadData();
                } else {
                    toast("Failed: Student ID not found");
                }
            }
        });

        // DELETE (dengan konfirmasi)
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String id = editId.getText().toString().trim();
                if (id.isEmpty()) {
                    toast("Enter or select a Student ID to delete");
                    return;
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete student")
                        .setMessage("Delete student with ID " + id + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (dbHelper.deleteStudent(id)) {
                                toast("Student deleted");
                                clearForm();
                                loadData();
                            } else {
                                toast("Failed: Student ID not found");
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        // VIEW
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
                toast(rowList.isEmpty() ? "No data yet" : "Showing " + rowList.size() + " student(s)");
            }
        });

        // Ketuk item di list -> isi form (untuk Edit / Delete)
        listStudents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                String[] row = rowList.get(position);
                editId.setText(row[0]);
                editName.setText(row[1]);
                checkEnrolled.setChecked(row[2].equals("1"));
            }
        });

        loadData();
    }

    private void loadData() {
        rowList.clear();
        int enrolledCount = 0;

        Cursor cursor = dbHelper.getAllStudents();
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
            int enrolled = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ENROLLED));

            rowList.add(new String[]{id, name, String.valueOf(enrolled)});
            if (enrolled == 1) enrolledCount++;
        }
        cursor.close();

        adapter.notifyDataSetChanged();
        textSummary.setText("Total: " + rowList.size() + "  |  Enrolled: " + enrolledCount);
    }

    private void clearForm() {
        editName.setText("");
        editId.setText("");
        checkEnrolled.setChecked(false);
    }

    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    // Adapter untuk tampilan tiap baris (avatar, nama, ID, badge status)
    class StudentAdapter extends ArrayAdapter<String[]> {

        StudentAdapter(Context context, ArrayList<String[]> data) {
            super(context, 0, data);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_student, parent, false);
            }

            String[] row = getItem(position);
            TextView itemAvatar = convertView.findViewById(R.id.itemAvatar);
            TextView itemName = convertView.findViewById(R.id.itemName);
            TextView itemId = convertView.findViewById(R.id.itemId);
            TextView itemBadge = convertView.findViewById(R.id.itemBadge);

            boolean enrolled = row[2].equals("1");

            itemAvatar.setText(row[1].substring(0, 1).toUpperCase()); // huruf pertama nama
            itemName.setText(row[1]);
            itemId.setText("ID: " + row[0]);
            itemBadge.setText(enrolled ? "Enrolled" : "Not Enrolled");
            itemBadge.setTextColor(Color.parseColor(enrolled ? "#166534" : "#991B1B"));
            itemBadge.setBackgroundTintList(ColorStateList.valueOf(
                    Color.parseColor(enrolled ? "#DCFCE7" : "#FEE2E2")));

            return convertView;
        }
    }
}