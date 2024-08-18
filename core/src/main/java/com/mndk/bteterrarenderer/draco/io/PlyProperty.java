//// A single PLY property of a given PLY element. For "vertex" element this can
//// contain data such as "x", "y", or "z" coordinate of the vertex, while for
//// "face" element this usually contains corner indices.
//class PlyProperty {
// public:
//  friend class PlyReader;
//
//  PlyProperty(const std::string &name, DataType data_type, DataType list_type);
//  void ReserveData(int num_entries) {
//    data_.reserve(DataTypeLength(data_type_) * num_entries);
//  }
//
//  int64_t GetListEntryOffset(int entry_id) const {
//    return list_data_[entry_id * 2];
//  }
//  int64_t GetListEntryNumValues(int entry_id) const {
//    return list_data_[entry_id * 2 + 1];
//  }
//  const void *GetDataEntryAddress(int entry_id) const {
//    return data_.data() + entry_id * data_type_num_bytes_;
//  }
//  void push_back_value(const void *data) {
//    data_.insert(data_.end(), static_cast<const uint8_t *>(data),
//                 static_cast<const uint8_t *>(data) + data_type_num_bytes_);
//  }
//
//  const std::string &name() const { return name_; }
//  bool is_list() const { return list_data_type_ != DT_INVALID; }
//  DataType data_type() const { return data_type_; }
//  int data_type_num_bytes() const { return data_type_num_bytes_; }
//  DataType list_data_type() const { return list_data_type_; }
//  int list_data_type_num_bytes() const { return list_data_type_num_bytes_; }
//
// private:
//  std::string name_;
//  std::vector<uint8_t> data_;
//  // List data contain pairs of <offset, number_of_values>
//  std::vector<int64_t> list_data_;
//  DataType data_type_;
//  int data_type_num_bytes_;
//  DataType list_data_type_;
//  int list_data_type_num_bytes_;
//};

package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import lombok.Getter;

@Getter
public class PlyProperty {

    private final CppVector<UByte> data = new CppVector<>(DataType.uint8());
    private final CppVector<Long> listData = new CppVector<>(DataType.int64());

    private final String name;
    private final DracoDataType dataType;
    private final int dataTypeNumBytes;
    private final DracoDataType listDataType;
    private final int listDataTypeNumBytes;

    public PlyProperty(String name, DracoDataType dataType, DracoDataType listDataType) {
        this.name = name;
        this.dataType = dataType;
        this.dataTypeNumBytes = (int) dataType.getDataTypeLength();
        this.listDataType = listDataType;
        this.listDataTypeNumBytes = (int) listDataType.getDataTypeLength();
    }

    public void reserveData(int numEntries) {
        data.reserve((long) numEntries * dataTypeNumBytes);
    }

    public long getListEntryOffset(int entryId) {
        return listData.get(entryId * 2L);
    }

    public long getListEntryNumValues(int entryId) {
        return listData.get(entryId * 2L + 1);
    }

    public <T> Pointer<T> getDataEntryAddress(int entryId, DataType<T> type) {
        return data.getRawPointer().rawAdd((long) entryId * dataTypeNumBytes).toType(type);
    }

    public <T> void pushBackValue(Pointer<T> data) {
        this.data.insert(this.data.size(), data.asRawToUByte(), dataTypeNumBytes);
    }

    public boolean isList() {
        return listDataType != DracoDataType.INVALID;
    }

}
