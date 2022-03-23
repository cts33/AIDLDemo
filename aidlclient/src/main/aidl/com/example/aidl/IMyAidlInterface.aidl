// IMyAidlInterface.aidl
package com.example.aidl;

// Declare any non-default types here with import statements
import com.example.aidl.Book;

interface IMyAidlInterface {

    String getString();

      List<Book> getBookList();

      void addBookInOut(in Book book);
}