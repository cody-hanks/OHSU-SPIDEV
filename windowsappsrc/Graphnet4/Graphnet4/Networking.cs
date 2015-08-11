using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.IO;
using System.Net.Sockets;

namespace Graphnet4
{
    class Networking
    {
        public Socket s = null;
        public IPHostEntry hostEntry = null;
        Byte[] bytesRECV = null;
        Form1.updatescreen updatecallback;
        public Networking(string hostname,Form1.updatescreen callback)
        {
            updatecallback = callback;
            hostEntry = Dns.GetHostEntry(hostname);
            IPEndPoint ipe = new IPEndPoint(hostEntry.AddressList[0], 5003);
            s = new Socket(ipe.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            s.Connect(ipe);
           
            bytesRECV = new Byte[256];
        }

        internal void run()
        {
            int bytes = 0;
            string text;
            string[] splits;
            string[] pair;
            int ch;
            long value;
            while (s.Connected)
            {
                bytes = s.Receive(bytesRECV, bytesRECV.Length, 0);
                text = System.Text.Encoding.UTF8.GetString(bytesRECV);
                splits = text.Split('\n');
                foreach (string split in splits)
                {
                    
                    pair = split.Split(',');
                    if (pair.Length > 1)
                    {
                        int.TryParse(pair[0], out ch);
                        long.TryParse(pair[1], out value);
                        updatecallback(ch, value);
                    }
                }
                Array.Clear(bytesRECV, 0, 256);
            }
            s.Close();
        }
    }
}
