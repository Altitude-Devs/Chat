package com.alttd.chat.objects;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface BatchInsertable {

    void prepareStatement(PreparedStatement preparedStatement) throws SQLException;

}
