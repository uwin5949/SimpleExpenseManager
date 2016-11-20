package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.db.DbConnect;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;


public class PersistantAccountDAO implements AccountDAO {
    private Context context;
    private DbConnect connector;

    public PersistantAccountDAO(Context  context) {
        this.context = context;
        connector = DbConnect.getInstance(context);
    }

    @Override
    public List<String>  getAccountNumbersList() {


        SQLiteDatabase db = connector.getReadableDatabase();

        String query = String.format("SELECT %s FROM %s ORDER BY %s ASC", connector.bankAccountNo, connector.accountTableName, connector.bankAccountNo);
        Cursor cursor = db.rawQuery(query , null);

        ArrayList<String> result = new ArrayList<String>();

        while(cursor.moveToNext())
        {
            result.add(cursor.getString(cursor.getColumnIndex(connector.bankAccountNo)));
        }
        cursor.close();
        return result;
    }

    @Override
    public List<Account> getAccountsList() {
        SQLiteDatabase db = connector.getReadableDatabase();

        String query = String.format("SELECT * FROM %s ORDER BY %s ASC", connector.accountTableName, connector.bankAccountNo);
        Cursor cursor = db.rawQuery(query , null);

        ArrayList<Account> result = new ArrayList<>();

        while(cursor.moveToNext())
        {
            Account account = new Account(cursor.getString(cursor.getColumnIndex(connector.bankAccountNo)),
                    cursor.getString(cursor.getColumnIndex(connector.bankName)),
                    cursor.getString(cursor.getColumnIndex(connector.accountHolder)),
                    cursor.getDouble(cursor.getColumnIndex(connector.balance)));

            result.add(account);
        }
        cursor.close();
        return result;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = connector.getReadableDatabase();

        String query = "SELECT * FROM " + connector.accountTableName + " WHERE " + connector.bankAccountNo + " =  '" + accountNo + "'";

        Cursor cursor = db.rawQuery(query , null);

        Account account = null;

        if(cursor.moveToFirst())
        {
            account = new Account(cursor.getString(cursor.getColumnIndex(connector.bankAccountNo)),
                    cursor.getString(cursor.getColumnIndex(connector.bankName)),
                    cursor.getString(cursor.getColumnIndex(connector.accountHolder)),
                    cursor.getDouble(cursor.getColumnIndex(connector.balance)));


        }
        else   {
            throw new InvalidAccountException("Entered Account No is invalid ...!");
        }
        cursor.close();
        return account;
    }

    @Override
    public void addAccount(Account account) {
        SQLiteDatabase db = connector.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(connector.accountNo, account.getAccountNo());
        values.put(connector.bankName, account.getBankName());
        values.put(connector.accountHolder, account.getAccountHolderName());
        values.put(connector.balance, account.getBalance());

        db.insert(connector.accountTableName, null, values);

    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {

        SQLiteDatabase db = connector.getWritableDatabase();

        String query = "SELECT * FROM " + connector.accountTableName + " WHERE " + connector.bankAccountNo + " =  '" + accountNo + "'";

        Cursor cursor = db.rawQuery(query, null);

        Account account = null;


        if (cursor.moveToFirst()) {
            account = new Account(cursor.getString(cursor.getColumnIndex(connector.bankAccountNo)),
                    cursor.getString(cursor.getColumnIndex(connector.bankName)),
                    cursor.getString(cursor.getColumnIndex(connector.accountHolder)),
                    cursor.getFloat(cursor.getColumnIndex(connector.balance)));
            db.delete(connector.accountTableName, connector.bankAccountNo + " = ?", new String[] { accountNo });
            cursor.close();

        }

        else {
            throw new InvalidAccountException("Account not found...!");
        }

    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        SQLiteDatabase db = connector.getWritableDatabase();

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

            String strSQL = "UPDATE "+ connector.accountTableName+" SET "+ connector.balance+" = "+new_amount+" WHERE "+ connector.bankAccountNo+" = '"+ accountNo+"'";

            db.execSQL(strSQL);

        }

        else {
            throw new InvalidAccountException("Account not found ...!");
        }


    }
}
