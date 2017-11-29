import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CurrentTimeDate
{
  private Calendar cal;

  public CurrentTimeDate()
  {
    this.cal = Calendar.getInstance();
  }

  public String getcurr()
  {
    this.cal = Calendar.getInstance();
    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    return dateFormat.format(this.cal.getTime()) + ": ";
  }
}