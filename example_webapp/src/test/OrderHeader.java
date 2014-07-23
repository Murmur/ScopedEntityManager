package test;

import java.util.*;
import javax.persistence.*;
import org.apache.openjpa.persistence.*;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;

@Entity
@Table(name="orderheader")
public class OrderHeader {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @Column(name="custid")
    private long custId;
	
    private String comment;

    @Column(name="updated_utc") @Temporal(TemporalType.TIMESTAMP) 
    @Factory("JPAUtils.db2cal") @Externalizer("JPAUtils.cal2db")
    private Calendar updated;

    // OpenJPA ElementJoinColumn custom tag provides
    // one-sided one-to-many link without using extra jointable.
    //    name=foreign field in child table, referencedColumnName=key field in master table
    //@ElementJoinColumn(name="headerid", referencedColumnName="id", nullable=false)
    @OneToMany(targetEntity=test.OrderRow.class,  // not mandatory in typed getter method
            fetch=FetchType.LAZY       // populate list on first getter call
            ,cascade=CascadeType.ALL	// deleting parent bean deletes associated childs
			,orphanRemoval=true			// delete orphan child rows
			)    
	@JoinColumn(name="headerid", referencedColumnName="id", nullable=false)
	@OrderBy("comment ASC")
    private List<OrderRow> rows;
    
    public long getId() { return id; }
    //public void setId(long id) { this.id = id; }

    public long getCustId() { return custId; }
    public void setCustId(long id) { this.custId = id; }

    public String getComment() { return comment; }
    public void setComment(String val) { comment=val; }

    public Calendar getUpdated() { return updated; }
    public void setUpdated(Calendar cal) { updated=cal; }

    public List<OrderRow> getRows() { return rows; }
    public void setRows(List<OrderRow> items) { rows=items; }
    
}
