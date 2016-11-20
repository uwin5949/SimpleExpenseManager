package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.dataBase.DataBaseConnect;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;



public class PersistanceTransactionDAO implements TransactionDAO {

    private Context context;
    private DataBaseConnect manager;
    public PersistanceTransactionDAO(Context context) {
        this.context = context;
        manager = DataBaseConnect.getInstance(context);

    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        SQLiteDatabase db = manager.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(manager.accountNo,accountNo);
        values.put(manager.date, dateFormatString(date));
        values.put(manager.amount, amount);
        values.put(manager.expenceType, expenseType.toString());
        db.insert(manager.transactionTableName , null, values);
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        return getPaginatedTransactionLogs(0);
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        SQLiteDatabase db = manager.getReadableDatabase();
        String querySize = String.format("SELECT count(accountNo) FROM %s ",manager.transactionTableName);
        Cursor cursorsize = db.rawQuery(querySize, null);
        int size = cursorsize.getCount();
        String query;
        if(size<=limit){
            query = "SELECT "+ manager.accountNo + ", " +
                    manager.date + ", " +
                    manager.expenceType+", " +
                    manager.amount +
                    " FROM " + manager.transactionTableName + " ORDER BY " + manager.transactionId + " DESC";
        }
        else {
            query = "SELECT "+ manager.accountNo + ", " +
                    manager.date + ", " +
                    manager.expenceType+", " +
                    manager.amount +
                    " FROM " + manager.transactionTableName + " ORDER BY " + manager.transactionId + " DESC LIMIT" + limit;
        }

        Cursor cursor = db.rawQuery(query,null);

        ArrayList<Transaction> transactionLogData = new ArrayList<>();

        while (cursor.moveToNext())
        {
            try{
                ExpenseType expenseType = null;
                if(cursor.getString(cursor.getColumnIndex(manager.expenceType)).equals(ExpenseType.INCOME.toString())){
                    expenseType = ExpenseType.INCOME;
                }
                else {
                    expenseType = ExpenseType.EXPENSE;
                }

                String dateString = cursor.getString(cursor.getColumnIndex(manager.date));
                Date date = dateFormatDate(dateString);
                Transaction transaction = new Transaction(date,cursor.getString(cursor.getColumnIndex(manager.accountNo)),
                        expenseType,
                        cursor.getDouble(cursor.getColumnIndex(manager.amount))
                );
                transactionLogData.add(transaction);
            }
            catch (ParseException e){
                e.printStackTrace();
            }

        }
        return transactionLogData;
    }


    public static String dateFormatString(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String dateString = dateFormat.format(date);
        return dateString;
    }

    public static Date dateFormatDate(String date) throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date strDate = dateFormat.parse(date);
        return strDate;
    }





}

