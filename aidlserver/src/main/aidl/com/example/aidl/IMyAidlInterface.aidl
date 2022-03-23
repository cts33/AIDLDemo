// IMyAidlInterface.aidl
package com.example.aidl;

import com.example.aidl.Book;

interface IMyAidlInterface {

    String getString();

    List<Book> getBookList();

    void addBookInOut(in Book book);
}