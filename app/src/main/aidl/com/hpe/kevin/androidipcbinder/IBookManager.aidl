// IBookManager.aidl
package com.hpe.kevin.androidipcbinder;

// Declare any non-default types here with import statements
import com.hpe.kevin.androidipcbinder.Book;

interface IBookManager {
    List<Book> getBookList();
    // 在aidl文件中所有非Java基本类型（需要实现Parcelable接口）参数必须加上in、out、inout标记，
    // 以指明参数是输入参数、输出参数还是输入输出参数。
    // Java原始类型默认的标记为in,不能为其它标记。
    void addBook(in Book book);
}
