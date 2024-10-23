package com.example.expensemanager;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.expensemanager.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.text.DateFormat;
import java.util.Date;

public class DashboardFragment extends Fragment {

    // Floating Buttons
    private FloatingActionButton fab_main, fab_income, fab_expense;
    private TextView fab_income_text, fab_expense_text;
    private boolean isOpen = false;
    private Animation fadeOpen, fadeClose;
    private TextView totalIncomResult, totalExpenseResult;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase, mExpenseDatabase;

    // Recycler Views
    private RecyclerView mRecyclerIncome, mRecyclerExpense;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);

        // Connect Floating Button to layout
        fab_main = myview.findViewById(R.id.fb_main_plus_btn);
        fab_income = myview.findViewById(R.id.income_ft_btn);
        fab_expense = myview.findViewById(R.id.expense_ft_btn);

        // Connect floating text
        fab_income_text = myview.findViewById(R.id.income_ft_text);
        fab_expense_text = myview.findViewById(R.id.expense_ft_text);

        // Total income and expense
        totalIncomResult = myview.findViewById(R.id.income_set_result);
        totalExpenseResult = myview.findViewById(R.id.expense_set_result);

        // Recycler
        mRecyclerIncome = myview.findViewById(R.id.recycler_income);
        mRecyclerExpense = myview.findViewById(R.id.recycler_expense);

        // Animations
        fadeOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        fadeClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);

        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();
                floatingButtonAnimation();
            }
        });

        setupRecyclerViews();
        calculateTotalIncome();
        calculateTotalExpense();

        return myview;
    }

    // Floating button animation
    private void floatingButtonAnimation() {
        if (isOpen) {
            fab_income.startAnimation(fadeClose);
            fab_expense.startAnimation(fadeClose);
            fab_income.setClickable(false);
            fab_expense.setClickable(false);
            fab_income_text.startAnimation(fadeClose);
            fab_expense_text.startAnimation(fadeClose);
            fab_income_text.setClickable(false);
            fab_expense_text.setClickable(false);
        } else {
            fab_income.startAnimation(fadeOpen);
            fab_expense.startAnimation(fadeOpen);
            fab_income.setClickable(true);
            fab_expense.setClickable(true);
            fab_expense_text.startAnimation(fadeOpen);
            fab_income_text.startAnimation(fadeOpen);
            fab_income_text.setClickable(true);
            fab_expense_text.setClickable(true);
        }
        isOpen = !isOpen;
    }

    private void addData() {
        fab_income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertIncomeData();
            }
        });

        fab_expense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertExpenseData();
            }
        });
    }

    private void insertIncomeData() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.custom_layout_for_insertdata, null);
        mydialog.setView(myview);
        final AlertDialog dialog = mydialog.create();
        dialog.setCancelable(false);

        EditText edtamount = myview.findViewById(R.id.amount);
        EditText edtType = myview.findViewById(R.id.type_edt);
        EditText edtNote = myview.findViewById(R.id.note_edt);

        Button saveBtn = myview.findViewById(R.id.btnSave);
        Button cancelBtn = myview.findViewById(R.id.btnCancel);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = edtType.getText().toString().trim();
                String amount = edtamount.getText().toString().trim();
                String note = edtNote.getText().toString().trim();

                if (TextUtils.isEmpty(type)) {
                    edtType.setError("Please Enter A Type");
                    return;
                }
                if (TextUtils.isEmpty(amount)) {
                    edtamount.setError("Please Enter Amount");
                    return;
                }
                if (TextUtils.isEmpty(note)) {
                    edtNote.setError("Please Enter A Note");
                    return;
                }
                int amountInInt = Integer.parseInt(amount);

                String id = mIncomeDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(amountInInt, type, note, id, mDate);
                mIncomeDatabase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Transaction Added Successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                floatingButtonAnimation();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                floatingButtonAnimation();
            }
        });
        dialog.show();
    }

    private void insertExpenseData() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.custom_layout_for_insertdata, null);
        mydialog.setView(myview);

        final AlertDialog dialog = mydialog.create();
        dialog.setCancelable(false);
        EditText edtamount = myview.findViewById(R.id.amount);
        EditText edttype = myview.findViewById(R.id.type_edt);
        EditText edtnote = myview.findViewById(R.id.note_edt);

        Button saveBtn = myview.findViewById(R.id.btnSave);
        Button cancelBtn = myview.findViewById(R.id.btnCancel);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = edtamount.getText().toString().trim();
                String type = edttype.getText().toString().trim();
                String note = edtnote.getText().toString().trim();

                if (TextUtils.isEmpty(type)) {
                    edttype.setError("Please Enter A Type");
                    return;
                }
                if (TextUtils.isEmpty(amount)) {
                    edtamount.setError("Please Enter Amount");
                    return;
                }
                if (TextUtils.isEmpty(note)) {
                    edtnote.setError("Please Enter A Note");
                    return;
                }
                int amountInInt = Integer.parseInt(amount);

                String id = mExpenseDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(amountInInt, type, note, id, mDate);
                mExpenseDatabase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Transaction Added Successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                floatingButtonAnimation();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                floatingButtonAnimation();
            }
        });
        dialog.show();
    }

    private void setupRecyclerViews() {
        LinearLayoutManager layoutManagerIncome = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerIncome.setStackFromEnd(true);
        layoutManagerIncome.setReverseLayout(true);
        mRecyclerIncome.setHasFixedSize(true);
        mRecyclerIncome.setLayoutManager(layoutManagerIncome);

        LinearLayoutManager layoutManagerExpense = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerExpense.setStackFromEnd(true);
        layoutManagerExpense.setReverseLayout(true);
        mRecyclerExpense.setHasFixedSize(true);
        mRecyclerExpense.setLayoutManager(layoutManagerExpense);

        loadIncomeData();
        loadExpenseData();
    }

    private void loadIncomeData() {
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mIncomeDatabase, Data.class)
                .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Data model) {
                holder.setType(model.getType());
                holder.setAmount(model.getAmount());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.dashboard_income, parent, false);
                return new MyViewHolder(view);
            }
        };

        mRecyclerIncome.setAdapter(adapter);
        adapter.startListening();
    }

    private void loadExpenseData() {
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class)
                .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Data model) {
                holder.setType(model.getType());
                holder.setAmount(model.getAmount());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.dashboard_expense, parent, false);
                return new MyViewHolder(view);
            }
        };

        mRecyclerExpense.setAdapter(adapter);
        adapter.startListening();
    }

    private void calculateTotalIncome() {
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalValue = 0;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Data data = snap.getValue(Data.class);
                    totalValue += data.getAmount();
                }
                totalIncomResult.setText(String.valueOf(totalValue));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DashboardFragment", "Income calculation error: " + error.getMessage());
            }
        });
    }

    private void calculateTotalExpense() {
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalValue = 0;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Data data = snap.getValue(Data.class);
                    totalValue += data.getAmount();
                }
                totalExpenseResult.setText(String.valueOf(totalValue));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DashboardFragment", "Expense calculation error: " + error.getMessage());
            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setType(String type) {
            TextView mType = mView.findViewById(R.id.type_txt_dashboard);
            mType.setText(type);
        }

        void setAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.amount_txt_dashboard);
            mAmount.setText(String.valueOf(amount));
        }

        void setNote(String note) {
            TextView mNote = mView.findViewById(R.id.note_txt_dashboard);
            mNote.setText(note);
        }

        void setDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_txt_dashboard);
            mDate.setText(date);
        }
    }
}
