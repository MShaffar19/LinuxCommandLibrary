package com.inspiredandroid.linuxcommandbibliotheca.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.google.firebase.analytics.FirebaseAnalytics
import com.inspiredandroid.linuxcommandbibliotheca.BuildConfig
import com.inspiredandroid.linuxcommandbibliotheca.R
import com.inspiredandroid.linuxcommandbibliotheca.models.CommandChildModel
import com.inspiredandroid.linuxcommandbibliotheca.models.CommandGroupModel
import com.inspiredandroid.linuxcommandbibliotheca.view.TerminalTextView

import java.util.HashMap

import butterknife.BindView
import butterknife.ButterKnife
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.row_scriptchild.view.*

/**
 * Created by simon on 23/01/17.
 */
class ScriptChildrenAdapter(data: OrderedRealmCollection<CommandGroupModel>?, autoUpdate: Boolean, private val mFirebaseAnalytics: FirebaseAnalytics) : RealmRecyclerViewAdapter<CommandGroupModel, ScriptChildrenAdapter.ScriptViewHolder>(data, autoUpdate) {

    private val expanded: HashMap<Int, Boolean>

    init {
        expanded = HashMap()
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ScriptViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_scriptchild, parent, false)
        return ScriptViewHolder(v)
    }

    override fun onBindViewHolder(scriptViewHolder: ScriptViewHolder, position: Int) {
        val item = data!![position]

        scriptViewHolder.bind(item);
    }

    private fun trackSelectContent(id: String?) {
        if (BuildConfig.DEBUG) {
            return
        }
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Basic Group")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    private fun isExpanded(position: Int): Boolean {
        return expanded.containsKey(position) && expanded[position]!!
    }

    /**
     * let user share the command with any compatible app
     *
     * @param command
     */
    private fun startShareActivity(context: Context, command: CommandChildModel) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(android.content.Intent.EXTRA_TEXT, command.command)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class ScriptViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: CommandGroupModel) {
            itemView.row_scriptgroup_tv_title.text = item.desc
            itemView.row_scriptgroup_iv_icon.setImageResource(item.imageResourceId)
            itemView.row_scriptgroup_ll_detail.removeAllViews()
            for (command in item.commands!!) {
                val v = LayoutInflater.from(itemView.context).inflate(R.layout.row_scriptchild_child, itemView.row_scriptgroup_ll_detail, false)

                val tv = v.findViewById<View>(R.id.row_scriptdescription_child_tv_description) as TerminalTextView
                tv.text = command.command
                tv.setCommands(CommandChildModel.getMans(command))

                val btn = v.findViewById<View>(R.id.row_scriptdescription_child_iv_share) as ImageButton
                btn.setOnClickListener { view -> startShareActivity(view.context, command) }

                itemView.row_scriptgroup_ll_detail.addView(v)
            }
            itemView.row_scriptgroup_ll_detail.visibility = if (isExpanded(position)) View.VISIBLE else View.GONE
            itemView.setOnClickListener { view ->
                expanded[position] = !isExpanded(position)
                notifyItemChanged(position)
                trackSelectContent(item.desc)
            }
        }
    }
}
