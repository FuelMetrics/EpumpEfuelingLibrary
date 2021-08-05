/** @file 	Epmsgprtcl.cpp
 * 
 * @brief 	A description of the above named module in "IOT_Firmware".
 *
 * @details
 * @date 	Jul 14, 2021
 * @author 	jcliff
 *
 * @par       
 *
 * 			COPYRIGHT NOTICE: (c) 2020 fuelmetrics.  All rights reserved.
 */

//#include <cstring>
//#include "Epmsgprtcl.h"
//#include "ep_main.h"


/**
 * SET preferences for library
 *
 * STRINGIFY_HEX_PACKETS
 * USE_AES_ENCRYP
 * */

#define STRINGIFY_HEX_PACKETS
#define USE_AES_ENCRYP

#define DELIM_PRE_1 (char)0xAB
#define DELIM_PRE_2 (char)0xCD

const int head_size = sizeof (MsgHead);
const int pckt_size = sizeof (MsgPacket);
const int pld_base_size = sizeof (MsgPayloadBase);

Ep_buffer::Ep_buffer(){
    init_state = false;
    buffHead = 0;
    buffTail = 0;
    err = 0;
    memset(rx_buff, 0, sizeof(rx_buff));
}
Ep_buffer::~Ep_buffer(){

}

int Ep_buffer::init(){
    if (init_state) return -1;

    init_state = true;
    return 0;
}

bool Ep_buffer::hasInit()
{
    return init_state;
}

int Ep_buffer::readd(char* data, s32 len, bool peek){
    if (isEmpty()) return -1;

    s32 availD = availData();
    len = (availD < len)? availD : len;

    s32 first_block, second_block;
    if ((buffTail +len) < MAX_BUFF_SIZE)
    {
        memcpy(data, rx_buff + buffTail, len);

        if (!peek) buffTail += len;
    }
    else
    {
        first_block = MAX_BUFF_SIZE - buffTail;
        second_block = len - first_block;
        memcpy(data, rx_buff+buffTail, first_block);
        memcpy(data+first_block, rx_buff, second_block);

        if (!peek) buffTail = second_block;
    }

    return len;

}

int Ep_buffer::llread(const char * data, s32 len){
    /** push the data into main buffer */
    if (len <= 0) return -1;
    if (isFull()) return -2;

    /**
     * If received data is less then available space
    * only get what can fill available space */
    s32 avail_space = availBuffSpace();
    len = (avail_space < len) ? avail_space : len;

    s32 first_block, second_block;

    /** checking available buffer space ensures that
    * data isn't copied beyond the tail */
    if ((buffHead +len) < MAX_BUFF_SIZE)
    {
        memcpy(rx_buff + buffHead, data, len);
        buffHead += len;
    }
    else
    {
        first_block = MAX_BUFF_SIZE - buffHead;
        second_block = len - first_block;
        memcpy(rx_buff+buffHead, data, first_block);
        memcpy(rx_buff, data+first_block, second_block);
        buffHead = second_block;
    }

    return 0;
}

int Ep_buffer::waitForResp(u32 millis){
    int avail = 0, ms_spent = 0;

    u32 current_tm = ep_get_current_ms();
    int ms_sleep_time = 10;
    do {
        ep_sleep_ms(ms_sleep_time);
        avail = availData();
        ms_spent = ep_get_duration_ms(current_tm);
    }while(avail <= 0 &&  ms_spent < millis);

    ep_log ("[%s] %dms of %dms, avail: %d", __FUNCTION__, ms_spent, (int)millis, avail);
    return avail;

}


void Ep_buffer::flush(){
    buffTail = 0;
    buffHead = buffTail;
}

s32 Ep_buffer::availData()
{
    /** Tail is where the device picks the data from
 * Head is what's moved ahead when there is data
 */
    s32 ln = 0;
    if(buffHead != buffTail){
        if (buffTail > buffHead)
            ln = (MAX_BUFF_SIZE - buffTail) +(buffHead);
        if (buffTail < buffHead)
            ln = buffHead - buffTail;
    }
    return ln;
}

bool Ep_buffer::isFull()
{
    /** Returns true if condition is true: android studio
     simplified it */
    return ( (buffTail == buffHead + 1) ||
             (buffHead == (buffTail + MAX_BUFF_SIZE - 1))
    );
}

bool Ep_buffer::isEmpty(){
    /** Returns true if condition is true: android studio
 simplified it */
    return buffTail == buffHead;
}

int Ep_buffer::availBuffSpace(){
    if (isEmpty()) return MAX_BUFF_SIZE-1;
    if (buffHead > buffTail)
        return (MAX_BUFF_SIZE-1-buffHead) + buffTail;

    return (buffTail - buffHead - 1);
}


void Ep_buffer::getErrStr(char* a){
    //TODO: not implemented yet
}


/**
 * ep Message parser
 * */
uint8_t glob_pump_name[PNM_SZ] = {0};
uint8_t glob_session_id[SID_SZ] = {0};
uint8_t glob_device_id[DID_SZ] = {0};

Ep_buffer ep_buff;      /** buffer */
extern ep_tx_cb ep_tx;  /** The callback to send message through */
uint8_t ep_last_tk_out__; /** XXX: for single thread applications */

int ep_set_head_param(char* pname, char* di, char* si)
{
	LOG_FN_ENTRY;
    //PUMP NAME: set pump char and number from (p1,p2, p3 etc.)
    char pChar = '\0';
    int pNum = 0;
    sscanf(pname, "%c%d", &pChar, &pNum);

    glob_pump_name[0] = pChar;
    glob_pump_name[1] = pNum;

    ep_log("[%s] pump name: %s, number: %d", __FUNCTION__, pname, glob_pump_name[1]);


    //DEVICE _ID: use default device id if di isn't provided
    uint8_t dvi[] = DEVICE_ID;
    if(di == NULL) di = (char*) dvi;
    memcpy(glob_device_id, di, sizeof(glob_device_id));

    //SESSION ID:
    if (si!= NULL) {
        memcpy(glob_session_id, si, sizeof(glob_session_id));
        return 0; //The end
    }

    time_t result = time(NULL);
    static int seedey = 2;
    if (result == (time_t) (-1)) {
        ep_log("[%s]Warning [bug alert in session management]: can't get time of device\n",__FUNCTION__);
        seedey++;
    }else {
        seedey = (intmax_t) result;
        ep_log("Session initialization time [%d] \n", seedey);
    }

    srand(seedey);
    int rn = rand();
    bcd_from_int(rn, glob_session_id, sizeof(glob_session_id));

    return 0;
}

/** cmd: new session */
int ep_cmd_new_session()
{
    LOG_FN_ENTRY;
    MsgPayloadNewSess payload;

    payload.base.id = EP_CMD_TY_NEW_SESSION;
    payload.base.len = 		0;

    /** processing response */
    MsgPacket resp_pkt;
    return ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));

}

/** cmd: authorise with amount */
int ep_cmd_authorise_amo(double amo)
{
	LOG_FN_ENTRY;
    MsgPayloadAuthAmo payload;

    payload.base.id = EP_CMD_TY_AUTH_AMO;
    payload.base.len = 		sizeof(payload.amo);

    bcd_from_float (amo, DEC_PLACE, payload.amo, sizeof(payload.amo));

    /** processing response */
    MsgPacket resp_pkt;
    return ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));
}

/** cmd: authorise with volume */
int ep_cmd_authorise_vol(double vol)
{
	LOG_FN_ENTRY;
    MsgPayloadAuthVol payload;

    payload.base.id = EP_CMD_TY_AUTH_VOL;
    payload.base.len = 		sizeof(payload.vol);

    bcd_from_float (vol, DEC_PLACE, payload.vol, sizeof(payload.vol));

    /** processing response */
    MsgPacket resp_pkt;
    return ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));
}

/** cmd: stop transaction  */
int ep_cmd_stop_trans()
{
	LOG_FN_ENTRY;
    MsgPayloadStopTrans payload;

    payload.base.id = EP_CMD_TY_END_TRANS;
    payload.base.len = 		0;

    /** processing response */
    MsgPacket resp_pkt;
    return ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));
}

int ep_cmd_verify_voucher(char* vch, int len)
{
	LOG_FN_ENTRY;
    MsgPayloadVerifyVch payload;

    uint8_t size_vch = sizeof(MsgPayloadVerifyVch) - sizeof(MsgPayloadBase);
    payload.base.id = EP_CMD_TY_VERIFY_VOUCHER;
    payload.base.len = 		size_vch;

//    bcd_from_numstring ((char *)vch, len, payload.voucher_num, sizeof(payload.voucher_num));
    strlcpy( (char*) payload.voucher_num, vch
    		, sizeof(payload.voucher_num));

    /** processing response */
    MsgPacket resp_pkt;
    //    if ( (resp->base.id == EP_RESP_TY_ACK) &&  ) return EP_ACK_TY_NO_ERROR;
//    if ( (resp->base.id == EP_RESP_TY_ACK) && (resp->tk != pckt.head.tk) ) return EP_ACK_TY_WRONG_TK;
//    if (  == EP_RESP_TY_UNACK ) return resp->type;
    return ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));
}

int ep_cmd_verify_remis(char* phoneNum, int plen, double value, uint8_t value_ty)
{
	LOG_FN_ENTRY;
    MsgPayloadVerifyRemis payload;

    uint8_t size_remis = sizeof(MsgPayloadVerifyRemis)
    		- sizeof(MsgPayloadBase);


    payload.base.id = EP_CMD_TY_VERIFY_REMIS;
    payload.base.len = 		 size_remis;

    //copy the phonenum or tag
    strlcpy( (char*) payload.PhoneNo, phoneNum
    		, sizeof(payload.PhoneNo));

    //copy value
    bcd_from_float (value, DEC_PLACE, payload.value, sizeof(payload.value));
    //value type: 'a': amo, 'v': vol
    payload.value_type = value_ty;

    /** processing response */
    MsgPacket resp_pkt;
    return ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));
}



int ep_cmd_verify_card(char* uid, int len)
{
	LOG_FN_ENTRY;

    MsgPayloadVerifyCard payload;

    uint8_t size_card = sizeof(payload.uid);
    payload.base.id = EP_CMD_TY_VERIFY_CARD;
    payload.base.len = 		 size_card;

    //copy uid
    payload.uid[0] = '\0';
    strlcpy((char*) payload.uid, uid, size_card - 1);

    /** processing response */
    MsgPacket resp_pkt;
    return ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));
}

int ep_cmd_get_pump_status(double* vol, double* amo, uint8_t * stt){
	LOG_FN_ENTRY;
    MsgPayloadGetPumpStatus payload;

    payload.base.id = EP_CMD_TY_GET_PUMP_STATUS;
    payload.base.len = 		 0;

    /** processing response */
    MsgPacket resp_pkt;
    int ret = ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));

    MsgPayloadPumpStatus *payl = (MsgPayloadPumpStatus *)resp_pkt.payload;

    *stt = payl->pm_st;
    *vol = bcd_to_float(payl->vol, sizeof(payl->vol), 2);
    *amo = bcd_to_float(payl->amo, sizeof(payl->amo), 2);

    return ret ;
};

int ep_cmd_get_trans_status(uint8_t * stt){
    LOG_FN_ENTRY;
    MsgPayloadGetPumpStatus payload;

    payload.base.id = EP_CMD_TY_GET_TRANS_STATUS;
    payload.base.len = 		 0;

    /** processing response */
    MsgPacket resp_pkt;
    int ret = ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));

    MsgPayloadTransStatus *payl = (MsgPayloadTransStatus *)resp_pkt.payload;

    if (payl->base.id == EP_RESP_TRANS_STATUS && stt!= NULL) *stt = payl->tr_st;

    return ret;
}

int ep_cmd_get_error_details(uint8_t * err)
{
    LOG_FN_ENTRY;
    MsgPayloadBase payload;

    payload.id = EP_CMD_TY_GET_ERROR_DETAILS;
    payload.len = 0;

    /** processing response */
    MsgPacket resp_pkt;
    int ret = ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));

    MsgPayloadErrorDetails *payl = (MsgPayloadErrorDetails *)resp_pkt.payload;

    if (payl->base.id == EP_RESP_ERROR_DETAILS && err != NULL) *err = payl->err_ty;

    return ret;
}

//int ep_cmd_get_error_details(uint8_t * err)
//{
//    LOG_FN_ENTRY;
//    MsgPayloadBase payload;
//
//    payload.id = EP_CMD_TY_GET_ERROR_DETAILS;
//    payload.len = 0;
//
//    /** processing response */
//    MsgPacket resp_pkt;
//    int ret = ep_msg_send_cmd( &payload, sizeof(payload), (uint8_t*)&resp_pkt, sizeof(resp_pkt));
//
//    MsgPayloadErrorDetails *payl = (MsgPayloadErrorDetails *)resp_pkt.payload;
//
//    if (payl->base.id == EP_RESP_ERROR_DETAILS && err != NULL) *err = payl->err_ty;
//
//    return ret;
//}

int ep_cmd_ack(uint8_t t)
{
    LOG_FN_ENTRY;
    MsgPayloadAck payload;

    payload.base.id = EP_RESP_TY_ACK;
    payload.base.len = 		 1;
    payload.tk = t;

    return ep_msg_send_only_cmd( &payload, sizeof(payload));
}

int ep_cmd_unack(uint8_t tk, uint8_t ty)
{
    LOG_FN_ENTRY;
    MsgPayloadAck payload;

    payload.base.id = EP_RESP_TY_UNACK;
    payload.base.len = 		 2;
    payload.tk = tk;
    payload.type = ty;

    return ep_msg_send_only_cmd( &payload, sizeof(payload));
}

int ep_resp_pump_status(uint8_t t, uint8_t st, float vol, float amo)
{
    LOG_FN_ENTRY;
    MsgPayloadPumpStatus payload;

    payload.base.id = EP_RESP_PUMP_STATUS;
    payload.base.len = sizeof(MsgPayloadPumpStatus) - sizeof(MsgPayloadBase);

    payload.tk = t;//token of message it is responding to
    payload.pm_st = st;

    bcd_from_float (vol, DEC_PLACE, payload.vol, sizeof(payload.vol));
    bcd_from_float (amo, DEC_PLACE, payload.amo, sizeof(payload.amo));

    return ep_msg_send_only_cmd( &payload, sizeof(payload));
}

int ep_resp_trans_status(uint8_t t, uint8_t st, uint8_t* data)
{
    LOG_FN_ENTRY;
    MsgPayloadTransStatus payload;

    payload.base.id = EP_RESP_TRANS_STATUS;
    payload.base.len = sizeof(MsgPayloadPumpStatus) - sizeof(MsgPayloadBase);
    payload.tk = t;//token of message it is responding to
    payload.tr_st = st;

    if(data!= NULL) memcpy(payload.data, data, sizeof(payload.data));

    return ep_msg_send_only_cmd( &payload, sizeof(payload));

}

int ep_resp_error_details(uint8_t tk, uint8_t err ) {
    LOG_FN_ENTRY;
    MsgPayloadErrorDetails payload;

    payload.base.id = EP_RESP_ERROR_DETAILS;
    payload.base.len = sizeof(MsgPayloadErrorDetails) - sizeof(MsgPayloadBase);
    payload.tk = tk;//token of message it is responding to
    payload.err_ty = err;

    return ep_msg_send_only_cmd(&payload, sizeof(payload));
}



int ep_msg_send_only_cmd(void *payload
        , int len)
{
    LOG_FN_ENTRY;
    uint32_t sizeOfPackt = 0;

    MsgPacket pckt; /**< full message packet object*/
    if (len > sizeof(pckt.payload) ) return -1;

    memcpy(pckt.payload, payload, len); /**< full message packet object */
    pckt.head.payload_len = len;

    buildHead(&pckt.head, &sizeOfPackt);

    ep_tx_only( (const char*)&pckt, (int)sizeOfPackt);

    

    return pckt.head.tk;
}

int ep_msg_send_cmd(void *payload
        , int len
        , uint8_t *resp_packet
        , int resp_out_size)
{
    LOG_FN_ENTRY;
    uint32_t sizeOfPackt = 0;

    MsgPacket pckt; /**< full message packet object*/
    if (len > sizeof(pckt.payload) ) return EP_ACK_TY_UNKNOWN_ERROR;

    memcpy(pckt.payload, payload, len); /**< full message packet object */
    pckt.head.payload_len = len;

    buildHead(&pckt.head, &sizeOfPackt);

    int ret = ep_tx_and_rx((const char*)&pckt, (int)sizeOfPackt
            , resp_packet, resp_out_size );

    MsgPacket* resp_pkt = (MsgPacket*)resp_packet;
    MsgPayloadAck *resp = (MsgPayloadAck*)resp_pkt->payload;
    ep_log("[%s]resp id: %d, resp tk: %x, sent tk: %x", __FUNCTION__,
           resp->base.id, resp->tk, pckt.head.tk);

    //link layer: disregard messages with mismatching tk
    if ( (ret > 0) && (resp->tk != pckt.head.tk) )
    {// if message got received and parsed fine
        buildResp(resp_packet, resp_out_size, EP_ERROR_WRONG_RESP);
    }

    return resp->base.id;
}


int ep_tx_only(const char* data, int len )
{
    LOG_FN_ENTRY;

    /** XXX: for single thread applications
     * store the tk sent out into a static memory
     */
    MsgPacket* pckt = (MsgPacket*)data; 
    ep_last_tk_out__ = pckt->head.tk; 

    /** Data is received as hex, convert to string */
    char data_str[500] = {0};
    int data_str_len = 0;

#if defined(STRINGIFY_HEX_PACKETS)
    hexToString((uint8_t*) data, len, (uint8_t*) data_str, sizeof(data_str));
    data_str_len = strlen(data_str);
#else
    data_str_len = sizeof(data_str);
    data_str_len = ( len > data_str_len )? data_str_len : len ;
    memcpy( data_str, data, data_str_len);
#endif

    ep_tx( data_str, data_str_len );
    return 0;
}

int ep_tx_and_rx(const char* data, int len, uint8_t *resp_payload, int resp_out_size, int timeout )
{
    LOG_FN_ENTRY;
    /** Send */
    ep_buff.flush();

    /** Data is received as hex, convert to string */
    ep_tx_only(data, len);

    /** Get */
    char data_in[100] = {0};
    int data_in_len;

    int ret = ep_buff.waitForResp( timeout );

    /** if data is not available*/
    if ( ret <= 1 ){
        buildResp(resp_payload
                , resp_out_size
                , EP_ERROR_NO_RESP);
        return -1;
    }

    /** if data is available: copy and parse data*/

    memset(data_in, 0, sizeof(data_in));

//            /** 1.a. just peak, do not clear read data (work on this later) */
//            ret = ep_buff.readd(data_in, sizeof(data_in), true);
    /** 1.a. read data out and clear from buffer */
    ret = ep_buff.readd(data_in, sizeof(data_in));
    ep_log("[%s] data_in: %s, ret: %d", __FUNCTION__, data_in, ret);

    /** 1.b. try parsing the data*/
    data_in_len = (ret>0)? ret: 0;

    return processMsgPacket(data_in, data_in_len, resp_payload, resp_out_size);

}

/** -------------------------
 * D2D Protocol specification: RESPONSE [GO -> app]
 * NB: no 16 or 32 bit values were stored,
 * All int, float and double are stored as bcd*/

uint32_t buildHead(MsgHead* hd, uint32_t *msg_out_size) {
    LOG_FN_ENTRY;

    /** For preamble  */
    hd->preamble[0] = DELIM_PRE_1;
    hd->preamble[1] = DELIM_PRE_2;

    /** For Devivce type  */
    hd->dt = EP_DEV_TY_GO_TEST; //Start with test 

    /** For device ID */
    memcpy(hd->di, glob_device_id, sizeof(hd->di));

    /** For session ID */
    memcpy(hd->si, glob_session_id, sizeof(hd->si));

    /** For token */
    static uint8_t tk = 1;
    hd->tk = tk++;

    /** For pump name */
    memcpy(hd->pn, glob_pump_name, sizeof(hd->pn));

    /** For length of packet to send */
    *msg_out_size = head_size + hd->payload_len;
    ep_log("[%s] ISSSSUUUE hd->pn 2[%x], glob: %x \n", __FUNCTION__, hd->pn[1], glob_pump_name[1]);
    return hd->tk;

}


int processMsgPacket(const char* data_str, int data_str_len
        , uint8_t* resp_out, int resp_out_size)
{
    LOG_FN_ENTRY;
    //	if to use hex string
    char data[300] = {0};
    int len = 0;

#if defined(STRINGIFY_HEX_PACKETS)
    len = hexFromString( (const uint8_t *) data_str, data_str_len
            , (uint8_t*)data, sizeof(data));
#else
    len = sizeof(data);
    len = (data_str_len > len)? len: data_str_len;
    memcpy(data, data_str, len);
#endif

    if (len < 0 ) {
        buildResp(resp_out, resp_out_size, EP_ERROR_CANT_CONVERT_MSG_STRING);
        return -1;
    }


    //work on this
    int ret = parseHead(data, len, resp_out, resp_out_size);

    ep_log("[%s] head parse ret [%d] \n", __FUNCTION__, ret);
    if (ret < 0){
        //return the error in parsing head via "resp_out"
        buildResp(resp_out, resp_out_size, EP_ERROR_IN_MSG_HEAD);
        return -2;
    }

    return ret;

}

int parseHead(const char* data, int len, uint8_t* resp_out, int resp_out_size){
    LOG_FN_ENTRY;
    /** must be able to get data for at least the head */
    MsgPacket* pckt;
    if ( len < head_size ) return -1;

    /** For preamble */
    int i;
    for (i=0; i< len-1; i++){ //it is len-1 because there are two characters to search for
        if ( (data[i] == DELIM_PRE_1) && (data[i+1] == DELIM_PRE_2) ) break;
    }

    ep_log("[%s] i: %d, len: %d, head_size: %d\n"
    , __FUNCTION__, i, len, head_size);
    if (i >= len-1) return -2;  //if can't find delim in up to the second to the last element in data

    int payload_len = len - head_size;
    if (payload_len < 2) return -3; //if payload can't be found

    len = (len > pckt_size)? pckt_size:len;
//    memcpy(&pckt, data, len);
    pckt = (MsgPacket*) data;
    if (payload_len != pckt->head.payload_len) return -4; // incomplete message

    /** For Devivce type */
//    hd->dt = EP_DEV_TY_GO;

    /** For device ID */
//    char dvi[4] = DEVICE_ID;

    /** For session ID */


    /** For length of response to app */
    buildResp(resp_out
            , resp_out_size
            , EP_CMD_TY_NULL
            , (void*)data
            , payload_len + head_size); //send out both the head and payload

    return 0;
}





int
buildResp(void* dst
        , int dst_size
        ,  Msg_CmdType id
        , void* src
        , int src_size
){

    uint8_t src_t[ head_size + pld_base_size] = {0};
    if (src == NULL) {
        MsgPayloadBase pBase;
        pBase.id = (uint8_t)id;
        pBase.len = 0;
        memcpy(src_t + head_size, &pBase, pld_base_size);

        src = src_t;
        src_size = sizeof(src_t);
    }

    dst_size = (dst_size < src_size)? dst_size: src_size;
    memcpy(dst, src, dst_size); //TODO: Come to make this safe by including the resp_out size in function arguments
    return 0;

}

