/*
// Copyright 2016 The Draco Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
#ifndef DRACO_CORE_DRACO_TYPES_H_
#define DRACO_CORE_DRACO_TYPES_H_

#include <stdint.h>

#include <string>

#include "draco/draco_features.h"

namespace draco {

enum DataType {
  // Not a legal value for DataType. Used to indicate a field has not been set.
  DT_INVALID = 0,
  DT_INT8,
  DT_UINT8,
  DT_INT16,
  DT_UINT16,
  DT_INT32,
  DT_UINT32,
  DT_INT64,
  DT_UINT64,
  DT_FLOAT32,
  DT_FLOAT64,
  DT_BOOL,
  DT_TYPES_COUNT
};

int32_t DataTypeLength(DataType dt);

// Equivalent to std::is_integral for draco::DataType. Returns true for all
// signed and unsigned integer types (including DT_BOOL). Returns false
// otherwise.
bool IsDataTypeIntegral(DataType dt);

}  // namespace draco

#endif  // DRACO_CORE_DRACO_TYPES_H_

namespace draco {

int32_t DataTypeLength(DataType dt) {
  switch (dt) {
    case DT_INT8:
    case DT_UINT8:
      return 1;
    case DT_INT16:
    case DT_UINT16:
      return 2;
    case DT_INT32:
    case DT_UINT32:
      return 4;
    case DT_INT64:
    case DT_UINT64:
      return 8;
    case DT_FLOAT32:
      return 4;
    case DT_FLOAT64:
      return 8;
    case DT_BOOL:
      return 1;
    default:
      return -1;
  }
}

bool IsDataTypeIntegral(DataType dt) {
  switch (dt) {
    case DT_INT8:
    case DT_UINT8:
    case DT_INT16:
    case DT_UINT16:
    case DT_INT32:
    case DT_UINT32:
    case DT_INT64:
    case DT_UINT64:
    case DT_BOOL:
      return true;
    default:
      return false;
  }
}

}  // namespace draco

 */

package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;

import javax.annotation.Nullable;

public enum DracoDataType {
    DT_INVALID(0, null),
    DT_INT8(1, DataType.int8()),
    DT_UINT8(2, DataType.uint8()),
    DT_INT16(3, DataType.int16()),
    DT_UINT16(4, DataType.uint16()),
    DT_INT32(5, DataType.int32()),
    DT_UINT32(6, DataType.uint32()),
    DT_INT64(7, DataType.int64()),
    DT_UINT64(8, DataType.uint64()),
    DT_FLOAT32(9, DataType.float32()),
    DT_FLOAT64(10, DataType.float64()),
    DT_BOOL(11, DataType.bool());

    public static final int DT_TYPES_COUNT = values().length;

    private final UByte id;
    @Getter
    @Nullable
    private final DataNumberType<?, ?> dataType;

    DracoDataType(int id, @Nullable DataNumberType<?, ?> dataType) {
        this.id = UByte.of(id);
        this.dataType = dataType;
    }

    public long getDataTypeLength() {
        return dataType == null ? -1 : dataType.size();
    }
    public boolean isDataTypeIntegral() {
        return dataType != null && dataType.isIntegral();
    }

    public static DracoDataType valueOf(UByte value) {
        for(DracoDataType type : values()) {
            if(type.id.equals(value)) return type;
        }
        return DT_INVALID;
    }
}
