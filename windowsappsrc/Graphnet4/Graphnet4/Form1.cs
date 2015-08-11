using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Threading;
using System.Windows.Forms;
using System.Windows.Forms.DataVisualization;
using System.Windows.Forms.DataVisualization.Charting;
using System.Net.Sockets;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.IO;
using System.Net.Sockets;

namespace Graphnet4
{
    public partial class Form1 : Form
    {
        System.Net.Sockets.TcpClient clientSocket;
        DataTable dt;
        Networking nt;
        public delegate void updatescreen(int ch,long value);
        DataRow newrow;
        int index;

        public Socket s = null;
        public IPHostEntry hostEntry = null;
        Byte[] bytesRECV = null;
        public Form1()
        {
            InitializeComponent();
        }
        private void Form1_Load(object sender, EventArgs e)
        {
            
            index = 0;
            dt = new DataTable();
            dt.Columns.Add("index");
            chart1.Series.Clear();
            newrow = dt.NewRow();
            for (int x = 0; x < 16; x++)
            {
                dt.Columns.Add(x.ToString());
                chart1.Series.Add(x.ToString());
                chart1.Series[x].ChartType = SeriesChartType.Line;
                chart1.Series[x.ToString()].XValueMember = "index";
                chart1.Series[x.ToString()].YValueMembers = x.ToString();

            }
            chart1.DataSource = dt;
            chart1.DataBind();
            //chart1.DataBindTable(dt,"other");        
        }

        private void button1_Click(object sender, EventArgs e)
        {
            disableclick();
            if (textBox1.TextLength < 3)
                MessageBox.Show("Host not found!");
            else
            {
                sendcmd("STRNET", textBox1.Text);
                updatescreen thisupdate = new updatescreen(updatedata);
                nt = new Networking(textBox1.Text, thisupdate);
                //MessageBox.Show(nt.hostEntry.AddressList[0].ToString());
                Thread rcvr = new Thread(new ThreadStart(nt.run));
                rcvr.Start();
            }
            button2.Enabled = true;
        }
        private void updatedata(int ch, long value)
        {
            if (ch == 1)
            {
                if(newrow != null)
                    dt.Rows.Add(newrow);
                newrow = dt.NewRow();
                newrow["index"] = index++;
                if (index > 100)
                    dt.Rows[0].Delete();
            }

            newrow[ch.ToString()] = value;
            chart1.Invoke(new MethodInvoker(delegate { chart1.DataBind(); }));
            //chart1.Dispatcher.BeginInvoke();

            //chart1.DataBind();
        }

        private void button2_Click(object sender, EventArgs e)
        {
            disableclick();
            if (textBox1.TextLength < 3)
                MessageBox.Show("Host not found!");
            else
            {
                sendcmd("STP", textBox1.Text);
            }
            enableclick();
        }
        private void sendcmd(string cmd,string hostname)
        {
            byte[] msgBuffer = new byte[256];
            hostEntry = Dns.GetHostEntry(hostname);
            IPEndPoint ipe = new IPEndPoint(hostEntry.AddressList[0], 5002);
            s = new Socket(ipe.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            s.Connect(ipe);
            s.ReceiveTimeout = 10;
            s.Send(Encoding.ASCII.GetBytes(cmd));
            try
            {
                int len = s.Receive(msgBuffer, msgBuffer.Length, 0);
                string text = System.Text.Encoding.UTF8.GetString(bytesRECV);
                toolStripStatusLabel1.Text = text;
            }
            catch (Exception ex) { }
            finally
            {
                s.Close();
            }
            
        }

        private void button3_Click(object sender, System.EventArgs e)
        {
            disableclick();
            if (textBox1.TextLength < 3)
                MessageBox.Show("Host not found!");
            else
            {
                sendcmd("LEDON", textBox1.Text);
            }
            enableclick();
        }

        private void button4_Click(object sender, System.EventArgs e)
        {
            disableclick();
            if (textBox1.TextLength < 3)
                MessageBox.Show("Host not found!");
            else
            {
                sendcmd("LEDOFF", textBox1.Text);
            }
            enableclick();
        }
        private void disableclick()
        {
            button1.Enabled = false;
            button2.Enabled = false;
            button3.Enabled = false;
            button4.Enabled = false;
        }
        private void enableclick()
        {
            button1.Enabled = true;
            button2.Enabled = true;
            button3.Enabled = true;
            button4.Enabled = true;
        }
    }
}
