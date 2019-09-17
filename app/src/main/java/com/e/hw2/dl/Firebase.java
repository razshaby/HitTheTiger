package com.e.hw2.dl;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Firebase {
    private static final String USERS_TABLE_NAME = "score_public_afeka";
    private DatabaseReference usersTable;


    public Firebase() {

        usersTable = FirebaseDatabase.getInstance().getReference(USERS_TABLE_NAME);

    }

    public DatabaseReference getUsersTable() {
        return usersTable;
    }
}
