package com.example.gradesaver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.example.gradesaver.R
import com.example.gradesaver.database.entities.Activity
import com.example.gradesaver.database.entities.Course

class StudentCoursesExpandableListAdapter(
    private val context: Context,
    private var allCourses: MutableList<Course>, // Holds all courses
    private var details: MutableMap<Course, List<Activity>>,
    private val onDeleteCourse: (Course) -> Unit
) : BaseExpandableListAdapter(), Filterable {

    private var filteredCourses = allCourses // Initially, all courses are shown

    override fun getGroup(groupPosition: Int) = filteredCourses[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int) = details[filteredCourses[groupPosition]]?.get(childPosition) ?: throw IllegalStateException("Activity not found")

    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int) = childPosition.toLong()

    override fun getGroupCount() = filteredCourses.size

    override fun getChildrenCount(groupPosition: Int) = details[filteredCourses[groupPosition]]?.size ?: 0

    override fun hasStableIds() = true

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.student_group_item, parent, false)
        val course = getGroup(groupPosition)
        view.findViewById<TextView>(R.id.course).text = course.courseName
        view.findViewById<ImageView>(R.id.ivDelete).setOnClickListener {
            onDeleteCourse(course)  // Trigger the deletion logic with confirmation
        }
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.student_child_item, parent, false)
        val activity = getChild(groupPosition, childPosition)
        view.findViewById<TextView>(R.id.activityName).text = activity.activityName
        view.findViewById<TextView>(R.id.activityDueDate).text = activity.dueDate.toString() // Format date appropriately
        return view
    }

    fun updateData(newCourses: MutableList<Course>, newDetails: MutableMap<Course, List<Activity>>) {
        allCourses.clear()
        allCourses.addAll(newCourses)
        details.clear()
        details.putAll(newDetails)
        notifyDataSetChanged()
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: MutableList<Course> = mutableListOf()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(allCourses)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim()
                    for (item in allCourses) {
                        if (item.courseName.toLowerCase().contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }
                return FilterResults().apply { values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCourses = results?.values as? MutableList<Course> ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}
