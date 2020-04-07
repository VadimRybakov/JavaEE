package ru.geekbrains.repositories;

import ru.geekbrains.config.DataSource;
import ru.geekbrains.entity.Product;


import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Named
public class ProductRepository {

    private Connection conn;

    @Inject
    private DataSource dataSource;

    @PostConstruct
    public void init() throws SQLException {
        this.conn = dataSource.getConnection();
        createTableIfNotExists(conn);

        if (findAll().size() == 0) {
            insert(new Product(-1L, "Product1", "Desc1", new BigDecimal(10)));
            insert(new Product(-1L, "Product2", "Desc2", new BigDecimal(102)));
            insert(new Product(-1L, "Product3", "Desc3", new BigDecimal(1030)));
            insert(new Product(-1L, "Product4", "Desc4", new BigDecimal(140)));
        }
    }

    public void insert(Product product) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "insert into products(name, description, price) values (?, ?, ?);")) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.execute();
        }
    }

    public void update(Product product) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "update products set name = ?, description = ?, price = ? where id = ?;")) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setLong(4, product.getId());
            stmt.execute();
        }
    }

    public void delete(long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "delete from products where id = ?;")) {
            stmt.setLong(1, id);
            stmt.execute();
        }
    }

    public Product findById(long id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "select id, name, description, price from products where id = ?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Product(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4));
            }
        }
        return new Product(-1L, "", "", null);
    }

    public List<Product> findAll() throws SQLException {
        List<Product> res = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("select id, name, description, price from products");

            while (rs.next()) {
                res.add(new Product(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4)));
            }
        }
        return res;
    }

    private void createTableIfNotExists(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute( "CREATE TABLE IF NOT EXISTS products\n" +
                    "(\n" +
                    "    id serial primary key,\n" +
                    "    name varchar(30),\n" +
                    "    description varchar(30),\n" +
                    "    price decimal(19, 2)\n" +
                    ");");
        }
    }
}
