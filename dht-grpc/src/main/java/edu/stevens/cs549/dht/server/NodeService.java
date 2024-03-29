package edu.stevens.cs549.dht.server;

import com.google.protobuf.Empty;
import edu.stevens.cs549.dht.activity.Dht;
import edu.stevens.cs549.dht.activity.DhtBase.Failed;
import edu.stevens.cs549.dht.activity.DhtBase.Invalid;
import edu.stevens.cs549.dht.activity.IDhtNode;
import edu.stevens.cs549.dht.activity.IDhtService;
import edu.stevens.cs549.dht.events.EventProducer;
import edu.stevens.cs549.dht.main.Log;
import edu.stevens.cs549.dht.rpc.*;
import edu.stevens.cs549.dht.rpc.DhtServiceGrpc.DhtServiceImplBase;
import edu.stevens.cs549.dht.rpc.NodeInfo;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Additional resource logic.  The Web resource operations call
 * into wrapper operations here.  The main thing these operations do
 * is to call into the DHT service object, and wrap internal exceptions
 * as HTTP response codes (throwing WebApplicationException where necessary).
 * 
 * This should be merged into NodeResource, then that would be the only
 * place in the app where server-side is dependent on JAX-RS.
 * Client dependencies are in WebClient.
 * 
 * The activity (business) logic is in the dht object, which exposes
 * the IDHTResource interface to the Web service.
 */

public class NodeService extends DhtServiceImplBase {
	
	private static final String TAG = NodeService.class.getCanonicalName();
	
	private static Logger logger = Logger.getLogger(TAG);

	/**
	 * Each service request is processed by a distinct service object.
	 *
	 * Shared state is in the state object; we use the singleton pattern to make sure it is shared.
	 */
	private IDhtService getDht() {
		return Dht.getDht();
	}
	
	// TODO: add the missing operations

	private void error(String mesg, Exception e) {
		logger.log(Level.SEVERE, mesg, e);
	}

	@Override
	public void getNodeInfo(Empty empty, StreamObserver<NodeInfo> responseObserver) {
		Log.weblog(TAG, "getNodeInfo()");
		responseObserver.onNext(getDht().getNodeInfo());
		responseObserver.onCompleted();
	}

	@Override
	public void getPred(Empty empty, StreamObserver<OptNodeInfo> responseObserver){
		Log.weblog(TAG, "getPred()");
		responseObserver.onNext(getDht().getPred());
		responseObserver.onCompleted();
	}

	@Override
	public void getSucc(Empty empty, StreamObserver<NodeInfo> responseObserver){
		Log.weblog(TAG,"getSucc()");
		responseObserver.onNext(getDht().getSucc());
		responseObserver.onCompleted();
	}

	@Override
	public void closestPrecedingFinger(Id id,StreamObserver<NodeInfo> responseObserver){
		Log.weblog(TAG,"closestPrecedingFinger()");
		responseObserver.onNext(getDht().closestPrecedingFinger(id.getId()));
		responseObserver.onCompleted();
	}

	@Override
	public void notify(NodeBindings nodeBindings, StreamObserver<OptNodeBindings> responseObserver){
		Log.weblog(TAG,"notify()");
		responseObserver.onNext(getDht().notify(nodeBindings));
		responseObserver.onCompleted();
	}

	@Override
	public void getBindings(Key key, StreamObserver<Bindings> responseObserver){
		Log.weblog(TAG,"getBindings()");
		try{
			List<String> list = Arrays.asList(getDht().get(key.getKey()));
			Bindings bindings = Bindings.newBuilder().setKey(key.getKey()).addAllValue(list).build();
			responseObserver.onNext(bindings);
		}catch (Invalid invalidException){
			Log.weblog(TAG,"The get Binding Failed");
			responseObserver.onError(invalidException);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void addBinding(Binding binding, StreamObserver<Empty> responseObserver){
		Log.weblog(TAG,"addBindings()");
		try{
			getDht().add(binding.getKey(),binding.getValue());
			responseObserver.onNext(Empty.getDefaultInstance());
			responseObserver.onCompleted();
		}catch (Invalid invalidException) {
			Log.weblog(TAG, "The Add Binding Failed");
			responseObserver.onError(invalidException);
		}
	}

	@Override
	public void deleteBinding(Binding binding, StreamObserver<Empty> responseObserver){
		Log.weblog(TAG,"deleteBinding()");
		try{
			getDht().delete(binding.getKey(),binding.getValue());
			responseObserver.onNext(Empty.getDefaultInstance());
			responseObserver.onCompleted();
		}catch (Invalid invalidException) {
			Log.weblog(TAG, "The Delete Binding Failed");
			responseObserver.onError(invalidException);
		}

	}

	@Override
	public void findSuccessor(Id id, StreamObserver<NodeInfo> responseObserver){
		Log.weblog(TAG, "findSuccessor()");
		try{
			responseObserver.onNext(getDht().findSuccessor(id.getId()));
			responseObserver.onCompleted();
		}catch (Failed failedException){
			Log.weblog(TAG, "The Find Successor Failed");
			responseObserver.onError(failedException);
		}
	}

	@Override
	public void listenOn(Subscription request,StreamObserver<Event> responseObserver){
		getDht().listenOn(request.getId(),request.getKey(),EventProducer.create(responseObserver));
	}

	@Override
	public void listenOff(Subscription request, StreamObserver<Empty> responseObserver){
		getDht().listenOff(request.getId(),request.getKey());
		responseObserver.onNext(Empty.getDefaultInstance());
		responseObserver.onCompleted();
	}


}