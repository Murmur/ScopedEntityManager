/**
 * Copyright: Tietovalo (http://)
 * @author Aki Nieminen
 * @version $Id$
 */
package test;

import java.util.Calendar;

import javax.persistence.*;

import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;

/**
 * 
 */
@Entity
@Table(name="orderrow")
public class OrderRow {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;
	
    @Column(name="headerid")	
    private long headerId;

    private String comment;

	@Column(name="qty")
    private int qty;

    @Column(name="updated_utc") @Temporal(TemporalType.TIMESTAMP)
    @Factory("JPAUtils.db2cal") @Externalizer("JPAUtils.cal2db")	
    private Calendar updated;

    public long getId() { return id; }
    //public void setId(long id) { this.id = id; }

    public long getHeaderId() { return headerId; }
    //public void setHeaderId(long id) { headerId = id; }

    public String getComment() { return comment; }
    public void setComment(String val) { comment=val; }
    
    public int getQuantity() { return qty; }
    public void setQuantity(int val) { qty=val; } 

    public Calendar getUpdated() { return updated; }
    public void setUpdated(Calendar cal) { updated=cal; }

}
