/** @file 	Epmsgprtcl.h
 * 
 * @brief 	A description of the above named module in "IOT_Firmware".
 *
 * @date 	Jul 14, 2021
 * @author 	jcliff
 *
 * @par       
 *
 * 			COPYRIGHT NOTICE: (c) 2021 fuelmetrics.  All rights reserved.
 *
 * @details
 * 			This source is the implementation in c++, the communication protocol
 * 			for efueling between a user app and the pump controller "GO"
 *
 * 			Features of this protocol
 *
 * 			1. Encryption
 *
 */

#ifndef EPUMPWIFITOOL_EP_MSG_HNDLR_H
#define EPUMPWIFITOOL_EP_MSG_HNDLR_H


//#include <cstdint>
//#include "ep_types.h"

#ifndef RX_TIMEOUT
#   define RX_TIMEOUT 400
#endif

#define PAYLOAD_SIZE    100

#define PNM_SZ 2		//Pump name size
#define SID_SZ 4		//Session id size
#define DID_SZ 4		//Device id size

#define MAX_BUFF_SIZE 1048
#define DEVICE_ID {7,7,3,4} //peculiar to each device
#define DEC_PLACE 2 //device

#define LOG_FN_ENTRY ep_log("[%s] Entry\n",__FUNCTION__)
#define LOG_FN_ENTRY_PRETTY ep_log("[%s] Entry\n", __PRETTY_FUNCTION__)

#define EP_REMIS_TY_AMO 0
#define EP_REMIS_TY_VOL 1

/** Initially planned to use C style oop, see _ep_depr_*/

/** TODO: The only CLASS in this library, Change later*/
class Ep_buffer{
    bool init_state;
    s32 err;
    char rx_buff[MAX_BUFF_SIZE];

    s32 buffHead;
    s32 buffTail;

public:
    //methods
    Ep_buffer();
    ~Ep_buffer();
    int init();

    /** Read data: Read data from buffer for operations */
    int readd(char* data, s32 len, bool peak = false);

    /** Low level read: Read data from hardware into buffer */
    int llread(const char* data, s32 len);

    int waitForResp(u32 millis); //TODO: not implemented yet

    void flush();

    s32 availData();


    bool hasInit();
    bool isFull();
    bool isEmpty();
    int availBuffSpace();

    void getErrStr(char* a); //TODO: not implemented yet


};


/**
 * Gets raw data, interprets it
 *
 */

/** Raw: */
/**  head */


/** To be used for versioning as well */
typedef enum{
    EP_DEV_TY_UNDEF = 1,
    EP_DEV_TY_GO_TEST,
    EP_DEV_TY_POS_TERMINAL
}MsgDeviceType;

typedef enum{
    EP_ACK_TY_NO_ERROR = 0,
	EP_ACK_TY_WRONG_TK,

	EP_UNACK_TY_WRONG_SESSION_ID,
	EP_UNACK_TY_UNDEF_MESSAGED,
	EP_UNACK_TY_REQUEST_IN_WRONG_STATE,
	EP_UNACK_TY_CORRUPT_PAYLOAD,
	EP_UNACK_TY_PUMP_NOT_FOUND,

	EP_ACK_TY_UNKNOWN_ERROR = -1,
}MsgACkType; //Used for both ack and unack

typedef enum{
    EP_CMD_TY_NULL = 0, //Not used for in any command
    EP_CMD_TY_NEW_SESSION = 1,
    EP_CMD_TY_AUTH_VOL,
    EP_CMD_TY_AUTH_AMO,
    EP_CMD_TY_VERIFY_VOUCHER,
    EP_CMD_TY_VERIFY_REMIS,
    EP_CMD_TY_VERIFY_CARD,
    EP_CMD_TY_GET_PUMP_STATUS,
    EP_CMD_TY_GET_TRANS_STATUS,
    EP_CMD_TY_GET_ERROR_DETAILS,
    EP_CMD_TY_END_TRANS,

	EP_CMD_TY_GET_TRANS_LOG,

    /** responses */
    EP_RESP_TY_ACK,
    EP_RESP_TY_UNACK,
    EP_RESP_PUMP_STATUS,
    EP_RESP_TRANS_STATUS,
    EP_RESP_ERROR_DETAILS,

    /** ERRORS */
	EP_ERROR_NO_RESP,
	EP_ERROR_WRONG_RESP,
    EP_ERROR_CANT_CONVERT_MSG_STRING,
    EP_ERROR_IN_MSG_HEAD,
}Msg_CmdType;

/** -------------------------
 * D2D Protocol specification: COMMANDS [app -> GO]
 * NB: no 16 or 32 bit values were stored,
 * All int, float and double are stored as bcd*/

/** header */
typedef struct{
    uint8_t preamble[2];	//just a unique 2 byte data
    uint8_t dt;         	//device type,  would also define memory architecture type (big endian or small endia)
    uint8_t di[4];			//device id [terminal id] | CHANGE THIS TO BCD AND REDUCE IT TO 8 BITS
    uint8_t si[SID_SZ];		//session id
    uint8_t tk;		    	//token: increments on every message going out
    uint8_t pn[2]; 			//pump number    index 0: P, 1: Number from 1 to 255
    uint8_t mt;				/**< message type: command, response, */
    uint8_t payload_len;   //several data type can be stringed together just like in ifsf
} MsgHead;  				//head size: 17

/** Packet */
typedef struct{
    MsgHead head;
    uint8_t payload[PAYLOAD_SIZE]; 	//maximum size of pyload
} MsgPacket;

/**
 * ----------------------------
 * Structs for each command type
 * */

typedef struct {
    uint8_t id;
    uint8_t len;
}MsgPayloadBase;

typedef struct { MsgPayloadBase base;
    //zero extra data
}MsgPayloadNewSess;

typedef struct { MsgPayloadBase base;
    uint8_t amo[5];
}MsgPayloadAuthAmo;

typedef struct { MsgPayloadBase base;
    uint8_t vol[5];
}MsgPayloadAuthVol;

typedef struct { MsgPayloadBase base;
    //No data for this (yet)
}MsgPayloadStopTrans;

typedef struct { MsgPayloadBase base;
    //No data for this (yet)
}MsgPayloadGetPumpStatus;
typedef struct { MsgPayloadBase base;
    //No data for this (yet)
}MsgPayloadGetTransStatus;


typedef struct { MsgPayloadBase base;
    uint8_t voucher_num[10];  //8 numeric characters
}MsgPayloadVerifyVch;

typedef struct { MsgPayloadBase base;
    uint8_t PhoneNo[16]; //16 numeric characters
    uint8_t value[5];
    uint8_t value_type; //'a': amo or 'v': vol
}MsgPayloadVerifyRemis;

typedef struct { MsgPayloadBase base;
    uint8_t uid[16];
}MsgPayloadVerifyCard;


/**responses */
typedef struct { MsgPayloadBase base;
    uint8_t tk; //token of message i'm responding to
    uint8_t type;	//mainly for unack, tells why message was unacknowledged
}MsgPayloadAck;	//used for both ack and unack, id just differs

typedef struct { MsgPayloadBase base;
    uint8_t tk;         //tk of message i'm responding to
    uint8_t pm_st;
    uint8_t vol[5]; 		//filled volume
    uint8_t amo[5];			//filled amount
}MsgPayloadPumpStatus;

typedef struct { MsgPayloadBase base;
    uint8_t tk;             //token of message i'm responding to
    uint8_t tr_st;			//transaction status (wifi status)
    uint8_t data[10];
}MsgPayloadTransStatus;

typedef struct { MsgPayloadBase base;
    uint8_t tk;
    uint8_t err_ty;
}MsgPayloadErrorDetails;

/**
 * ----------------------------
 * Functions for each commands
 * */

int ep_set_head_param(char* pname, char* di = NULL, char* si = NULL);

/** Level 1 */
/** cmd: new session */
int ep_cmd_new_session();

/** payload: authorize amount */
int ep_cmd_authorise_amo(double a);

/** payload: authorize volume */
int ep_cmd_authorise_vol(double v);

/** payload: stop transaction */
int ep_cmd_stop_trans();

/** payload: verify voucher [vch has to be an int string ] */
int ep_cmd_verify_voucher(char* vch, int len);

/** payload: verify remis transction,
 * value: volume or amount */
int ep_cmd_verify_remis(char* phonenum, int plen, double value, uint8_t value_ty);

/** payload: verify card */
int ep_cmd_verify_card(char* uid, int len);

/** payload: get pump status */
int ep_cmd_get_pump_status(double* vol, double* amo, uint8_t * stt);

/** payload: get trans status */
int ep_cmd_get_trans_status(uint8_t * stt = NULL);

/** payload: get error details */
int ep_cmd_get_error_details(uint8_t * err = NULL);

/** payload: get Past transaction log
 * param[in] id	 index of the last transaction    	*/
int ep_cmd_get_trans_log(uint8_t id, uint8_t* trans_ty, uint8_t * trans_uid_out);

/** payload: acknowledge command received  */
int ep_cmd_ack(uint8_t tk);

/** payload: unacknowledge command received  */
int ep_cmd_unack(uint8_t t, uint8_t ty);

/** payload: return pump status  */
int ep_resp_pump_status(uint8_t t, uint8_t st, float vol, float amo);

/** payload: return transaction status  */
int ep_resp_trans_status(uint8_t t, uint8_t st, uint8_t* data);

/** payload: return transaction status  */
int ep_resp_error_details(uint8_t tk, uint8_t err );


/** -------------------------------
 * Build messages to send */
/**
 * Protocol Level: 2
 * @param payload
 * @param len
 * @param resp_payload
 * @param resp_out_size
 * @return	TK of sent message/command
 */
int ep_msg_send_only_cmd(void *payload
        , int len);

int ep_msg_send_cmd(void *payload
        , int len
        , uint8_t *resp_packet
        , int resp_out_size);

uint32_t buildHead(MsgHead* hd, uint32_t *msg_out_size);


/**
 * Protocol Level: 3 a
 * Just sends the command and not wait for a response
 *
 * @param data
 * @param len
 * @param resp_payload
 * @param resp_out_size
 * @param timeout           default value of 400ms
 * @return   0
 */
int ep_tx_only(const char* data, int len);

/**
 * Protocol Level: 3 b
 * Sends message and waits for a response
 *
 * @param data
 * @param len
 * @param resp_payload
 * @param resp_out_size
 * @param timeout           default value of 400ms
 */
int ep_tx_and_rx(const char* data
        , int len
        , uint8_t *resp_payload
        , int resp_out_size
        , int timeout = RX_TIMEOUT);


/**
 * Protocol Level 4
 *
 * @param   resp_out     	array of char to return to the
 * @param   resp_out_size  	size of array of char
 * @return  >0           	size of data to send out
 *          0       	   	error that has been built into response
 */
int processMsgPacket(const char* data, int len, uint8_t* resp_out, int resp_out_size);
int parseHead(const char* data, int len, uint8_t* resp_out, int resp_out_size);

/**
 * Builds the resp out pointer with the payload id or the src pointer
 *
 * If the src pointer is not null it builds with that
 *
 * @param dst
 * @param dst_size
 * @param id        [optional] use this or the src pointer
 * @param src       [optional] use this or the id
 * @param src_size
 * @return
 */
int
buildResp(void* dst
        , int dst_size
        ,  Msg_CmdType id
        , void* src = NULL
        , int src_size = 0
);

#endif //EPUMPWIFITOOL_EP_MSG_HNDLR_H



