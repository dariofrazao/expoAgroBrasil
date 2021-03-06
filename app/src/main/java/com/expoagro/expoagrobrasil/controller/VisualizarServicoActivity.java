package com.expoagro.expoagrobrasil.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.expoagro.expoagrobrasil.R;
import com.expoagro.expoagrobrasil.dao.ServicoDAO;
import com.expoagro.expoagrobrasil.dao.UserDAO;
import com.expoagro.expoagrobrasil.model.Servico;
import com.expoagro.expoagrobrasil.model.Usuario;
import com.expoagro.expoagrobrasil.util.GoogleSignIn;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Fabricio on 8/4/2017.
 */

public class VisualizarServicoActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private ProgressDialog progress;
    private GoogleApiClient mGoogleApiClient;
    private String shareServico;
    private static String idAnunciante;
    private Servico servico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_servico);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar7);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String keyServico = MenuServicoActivity.getId();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        mGoogleApiClient = new GoogleApiClient.Builder(VisualizarServicoActivity.this)
                .enableAutoManage(VisualizarServicoActivity.this, VisualizarServicoActivity.this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        idAnunciante = null;

        progress = new ProgressDialog(VisualizarServicoActivity.this);
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        progress.setMessage("Carregando anúncio...");
        progress.show();

        ServicoDAO.getDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot serv : dataSnapshot.getChildren()) {
                    if (serv.getKey().equals(keyServico)) {
                        servico = serv.getValue(Servico.class);
                        ((TextView) findViewById(R.id.dataServico)).setText("Publicado em: " + servico.getData());
                        ((TextView) findViewById(R.id.descricaoServico)).setText("Descrição: " + servico.getDescricao());
                        ((TextView) findViewById(R.id.nomeServico)).setText("Nome: " + servico.getNome());
                        ((TextView) findViewById(R.id.observacaoServico)).setText("Observação: " + servico.getObservacao());
                        UserDAO.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot user : dataSnapshot.getChildren()) {
                                    if (user.getKey().equals(servico.getIdUsuario())) {
                                        final Usuario target = user.getValue(Usuario.class);
                                        ((TextView) findViewById(R.id.vendedorServico)).setText("Vendedor: " + target.getNome());
                                        ((TextView) findViewById(R.id.vendedorServico)).setTextColor(getResources().getColor(R.color.colorAccent));
                                        ((TextView) findViewById(R.id.vendedorServico)).setTypeface(null, Typeface.BOLD);
                                        findViewById(R.id.vendedorServico).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                setIdAnunciante(target.getId());
                                                Intent intent = new Intent(VisualizarServicoActivity.this, VisualizarAnuncianteActivity.class);
                                                startActivity(intent);
                                            }
                                        });
                                        progress.dismiss();
                                        break;
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) { System.out.println("Error - recuperar usuario - Visualizar Anuncio"); }
                        });
                        shareServico = "Confira " + servico.getNome().toUpperCase() + " por " + servico.getValor() + " no aplicativo ExpoAgro Brasil!";
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { System.out.println("Visualizar Servico database error"); }
        });

        TextView verComentarios = (TextView) findViewById(R.id.textoComentarios);
        verComentarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent telaComentarios = new Intent(VisualizarServicoActivity.this, ComentariosActivity.class);
                startActivity(telaComentarios);
            }
        });

        ImageButton mBtnServico = (ImageButton) findViewById(R.id.btnCompartilharServico);
        mBtnServico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                String shareBody = shareServico + " https://play.google.com/store?hl=pt-BR&tab=w8";
                String shareSub = "Baixe o aplicativo ExpoAgro Brasil e confira a oferta!";
                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
                startActivity(Intent.createChooser(myIntent, "Compartilhar usando"));
            }
        });

        favoritar(keyServico);

        Button alterar = (Button) findViewById(R.id.alterarServico);
        Button excluir = (Button) findViewById(R.id.excluirServico);

        alterar.setVisibility(View.GONE);
        excluir.setVisibility(View.GONE);

        checkForConnection();
    }

    private void favoritar(String keyServico) {
        final ImageButton mBtnFavoritoServico = (ImageButton) findViewById(R.id.btnFavoritarServico);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            mBtnFavoritoServico.setEnabled(false);
        } else {
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Query ref = FirebaseDatabase.getInstance().getReference("Favoritos").child(uid).child(keyServico);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        mBtnFavoritoServico.setImageResource(R.drawable.if_star_285661);
                        mBtnFavoritoServico.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FirebaseDatabase.getInstance().getReference("Favoritos").child(uid).child(servico.getId()).removeValue();
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        });
                    } else {
                        mBtnFavoritoServico.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mBtnFavoritoServico.setImageResource(R.drawable.if_star_285661);
                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                FirebaseDatabase.getInstance().getReference("Favoritos").child(uid).child(servico.getId()).setValue(servico);
                                /*atualização da activity*/
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) { System.out.println(databaseError.getMessage()); }
            });

            mBtnFavoritoServico.setVisibility(View.VISIBLE);

        }
    }

    private void checkForConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean isConnected =  netInfo != null && netInfo.isConnectedOrConnecting();
        if (!isConnected) {
            Toast.makeText(VisualizarServicoActivity.this, "Você não está conectado a Internet", Toast.LENGTH_SHORT).show();
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                GoogleSignIn.signOut(VisualizarServicoActivity.this, mGoogleApiClient);
            }
            progress.dismiss();
            finish();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("onConnection Failed Listener");
    }

    public static void setIdAnunciante(String id) {
        idAnunciante = id;
    }

    public static String getIdAnunciante() {
        return idAnunciante;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MenuServicoActivity.setId(null);
//        Intent intent = new Intent(VisualizarServicoActivity.this, VisualizarMeusServicosActivity.class);
//        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MenuServicoActivity.setId(null);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
