package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.dataBase.DataBaseConnect;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;


public class PersistanceAccountDAO implements AccountDAO {
    private Context context;
    private DataBaseConnect manager;

    public PersistanceAccountDAO(Context context) {
        this.context = context;
        manager = DataBaseConnect.getInstance(context);
    }

    @Override
    public List<String> getAccountNumbersList() {


        SQLiteDatabase db = manager.getReadableDatabase();

        String query = String.format("SELECT %s FROM %s ORDER BY %s ASC",manager.bankAccountNo,manager.accountTableName,manager.bankAccountNo);
        Cursor cursor = db.rawQuery(query , null);

        ArrayList<String> result = new ArrayList<String>();

        while(cursor.moveToNext())
        {
            result.add(cursor.getString(cursor.getColumnIndex(manager.bankAccountNo)));
        }
        cursor.close();
        return result;
    }

    @Override
    public List<Account> getAccountsList() {
        SQLiteDatabase db = manager.getReadableDatabase();

        String query = String.format("SELECT * FROM %s ORDER BY %s ASC",manager.accountTableName,manager.bankAccountNo);
        Cursor cursor = db.rawQuery(query , null);

        ArrayList<Account> result = new ArrayList<>();

        while(cursor.moveToNext())
        {
            Account account = new Account(cursor.getString(cursor.getColumnIndex(manager.bankAccountNo)),
                    cursor.getString(cursor.getColumnIndex(manager.bankName)),
                    cursor.getString(cursor.getColumnIndex(manager.accountHolder)),
                    cursor.getDouble(cursor.getColumnIndex(manager.balance)));

            result.add(account);
        }
        cursor.close();
        return result;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = manager.getReadableDatabase();

        String query = "SELECT * FROM " + manager.accountTableName + " WHERE " + manager.bankAccountNo + " =  '" + accountNo + "'";

        Cursor cursor = db.rawQuery(query , null);

        Account account = null;

        if(cursor.moveToFirst())
        {
            account = new Account(cursor.getString(cursor.getColumnIndex(manager.bankAccountNo)),
                    cursor.getString(cursor.getColumnIndex(manager.bankName)),
                    cursor.getString(cursor.getColumnIndex(manager.accountHolder)),
                    cursor.getDouble(cursor.getColumnIndex(manager.balance)));


        }
        else   {
            throw new InvalidAccountException("Entered Account No is invalid ...!");
        }
        cursor.close();
        return account;
    }

    @Override
    public void addAccount(Account account) {
        SQLiteDatabase db = manager.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(manager.accountNo, account.getAccountNo());
        values.put(manager.bankName, account.getBankName());
        values.put(manager.accountHolder, account.getAccountHolderName());
        values.put(manager.balance, account.getBalance());

        db.insert(manager.accountTableName, null, values);

    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {

        SQLiteDatabase db = manager.getWritableDatabase();

        String query = "SELECT * FROM " + manager.accountTableName + " WHERE " + manager.bankAccountNo + " =  '" + accountNo + "'";

        Cursor cursor = db.rawQuery(query, null);

        Account account = null;


        if (cursor.moveToFirst()) {
            account = new Account(cursor.getString(cursor.getColumnIndex(manager.bankAccountNo)),
                    cursor.getString(cursor.getColumnIndex(manager.bankName)),
                    cursor.getString(cursor.getColumnIndex(manager.accountHolder)),
                    cursor.getFloat(cursor.getColumnIndex(manager.balance)));
            db.delete(manager.accountTableName, manager.bankAccountNo + " = ?", new String[] { accountNo });
            cursor.close();

        }

        else {
            throw new InvalidAccountException("Account not found...!");
        }

    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        SQLiteDatabase db = manager.getWritableDatabase();

        ContentValues values = new ContentValues();

        Account account = getAccount(accountNo);

        if (account!=null) {

            double new_amount=0;

            if (expenseType.equals(ExpenseType.EXPENSE)) {
                new_amount = account.getBalance() - amount;
            }

            else if (expenseType.equals(ExpenseType.INCOME)) {
                new_amount = account.getBalance() + amount;
            }

            String strSQL = "UPDATE "+manager.accountTableName+" SET "+manager.balance+" = "+new_amount+" WHERE "+manager.bankAccountNo+" = '"+ accountNo+"'";

            db.execSQL(strSQL);

        }

        else {
            throw new InvalidAccountException("Account not found ...!");
        }


    }
}
