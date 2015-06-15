#ifndef BED_SAMPLER_H
#define BED_SAMPLER_H


class BED_Sampler
{
    public:
        struct samplerow{
            int samples[16];
            int cnt;
        };
        struct samplelists{
            samplerow *lst1;
            samplerow *lst2;
            timeval *lst1time;
            timeval *lst2time;
        };
        BED_Sampler();
        virtual ~BED_Sampler();
    protected:
    private:
        int _debuglevel;
        static const char *_spi_name= "/dev/spidev1.0";
        static const long _lstsize = 4096;
        bool *_stop;





};

#endif // BED_SAMPLER_H
